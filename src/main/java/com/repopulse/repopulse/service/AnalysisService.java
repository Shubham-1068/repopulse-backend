package com.repopulse.repopulse.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.repopulse.repopulse.dto.AnalysisRequest;
import com.repopulse.repopulse.dto.OpenRouter;
import com.repopulse.repopulse.entity.RepoAnalysis;
import com.repopulse.repopulse.entity.User;
import com.repopulse.repopulse.repository.Report;
import com.repopulse.repopulse.repository.UserRepository;
import org.springframework.stereotype.Service;
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
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String googleToken = authHeader.replace("Bearer ", "");
        String email = googleAuthService.verify(googleToken);

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    return userRepository.save(newUser);
                });

        String repo = req.getRepoName();
        String githubToken = req.getToken();

        ObjectMapper mapper = new ObjectMapper();

        String openPrJson = GithubService.fetchPRs(repo, githubToken);
        JsonNode openRoot = mapper.readTree(openPrJson);

        int totalOpenPRs = openRoot.size();
        int stalePRs = 0;

        for (JsonNode pr : openRoot) {
            String createdAt = pr.get("created_at").asText();
            OffsetDateTime createdDate = OffsetDateTime.parse(createdAt);

            if (createdDate.isBefore(
                    OffsetDateTime.now(ZoneOffset.UTC).minusDays(30))) {
                stalePRs++;
            }
        }

        String closedPrJson = GithubService.fetchClosedPRs(repo, githubToken);
        JsonNode closedRoot = mapper.readTree(closedPrJson);

        int closedPRs = closedRoot.size();
        int mergedPRs = 0;

        for (JsonNode pr : closedRoot) {
            if (!pr.get("merged_at").isNull()) {
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
}