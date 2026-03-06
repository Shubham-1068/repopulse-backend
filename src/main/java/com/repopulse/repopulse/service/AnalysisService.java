package com.repopulse.repopulse.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.repopulse.repopulse.dto.AnalysisRequest;
import com.repopulse.repopulse.dto.OpenRouter;
import com.repopulse.repopulse.entity.RepoAnalysis;
import com.repopulse.repopulse.entity.User;
import com.repopulse.repopulse.repository.Report;
import com.repopulse.repopulse.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class AnalysisService {

    private final Report report;
    private final OpenRouterService openRouterService;
    private final GoogleAuthService googleAuthService;
    private final UserRepository userRepository;

    public AnalysisService(Report repoAnalysisRepository,
                           OpenRouterService openRouterService,
                           GoogleAuthService googleAuthService,
                           UserRepository userRepository) {

        this.report = repoAnalysisRepository;
        this.openRouterService = openRouterService;
        this.googleAuthService = googleAuthService;
        this.userRepository = userRepository;
    }

    public RepoAnalysis analyze(AnalysisRequest req, String authHeader) throws Exception {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        if (req == null || req.getRepoName() == null || req.getRepoName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "repoName is required");
        }

        String googleToken = authHeader.substring(7);
        String email = googleAuthService.verify(googleToken);

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    return userRepository.save(newUser);
                });

        String repo = req.getRepoName().trim();

        String githubToken = req.getToken();
        if (githubToken == null || githubToken.isBlank()) {
            githubToken = System.getenv("GITHUB_TOKEN");
        }

        ObjectMapper mapper = new ObjectMapper();

        String openPrJson = GithubService.fetchPRs(repo, githubToken);
        JsonNode openRoot = mapper.readTree(openPrJson);

        if (!openRoot.isArray()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unexpected GitHub PR response: " + extractGithubError(openRoot)
            );
        }

        int totalOpenPRs = openRoot.size();
        int stalePRs = 0;

        for (JsonNode pr : openRoot) {
            JsonNode createdAtNode = pr.get("created_at");
            if (createdAtNode == null || createdAtNode.isNull()) {
                continue;
            }

            String createdAt = createdAtNode.asText();
            OffsetDateTime createdDate = OffsetDateTime.parse(createdAt);

            if (createdDate.isBefore(
                    OffsetDateTime.now(ZoneOffset.UTC).minusDays(30))) {
                stalePRs++;
            }
        }

        String closedPrJson = GithubService.fetchClosedPRs(repo, githubToken);
        JsonNode closedRoot = mapper.readTree(closedPrJson);

        if (!closedRoot.isArray()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unexpected GitHub closed PR response: " + extractGithubError(closedRoot)
            );
        }

        int closedPRs = closedRoot.size();
        int mergedPRs = 0;

        for (JsonNode pr : closedRoot) {
            JsonNode mergedAtNode = pr.get("merged_at");
            if (mergedAtNode != null && !mergedAtNode.isNull()) {
                mergedPRs++;
            }
        }

        int unmergedClosedPRs = closedPRs - mergedPRs;

        RepoAnalysis analysis = new RepoAnalysis();
        analysis.setUser(user);
        analysis.setRepoName(repo);
        analysis.setTotalPRs(totalOpenPRs);
        analysis.setStalePRs(stalePRs);
        analysis.setClosedPRs(closedPRs);
        analysis.setMergedPRs(mergedPRs);
        analysis.setUnmergedClosedPRs(unmergedClosedPRs);

        int healthScore = 100;
        healthScore -= stalePRs * 3;
        healthScore -= unmergedClosedPRs * 2;

        analysis.setHealthScore(Math.max(0, healthScore));
        analysis.setAnalyzedAt(java.time.LocalDateTime.now());

        try {
            OpenRouter aiResponse = openRouterService.generateRecommendation(analysis);
            analysis.setAiRecommendations(
                    String.join("\n", aiResponse.getRecommendations())
            );
        } catch (Exception e) {
            analysis.setAiRecommendations("AI recommendation unavailable.");
        }

        return report.save(analysis);
    }

    public java.util.List<RepoAnalysis> getUserHistory(String authHeader) throws Exception {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        String googleToken = authHeader.substring(7);
        String email = googleAuthService.verify(googleToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return report.findByUserOrderByAnalyzedAtDesc(user);
    }

    private static String extractGithubError(JsonNode root) {
        JsonNode messageNode = root.get("message");
        if (messageNode != null && !messageNode.isNull()) {
            return messageNode.asText();
        }
        return root.toString();
    }
}