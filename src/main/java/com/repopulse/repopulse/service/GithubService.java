package com.repopulse.repopulse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GithubService {
    private static final RestTemplate restTemplate = new RestTemplate();

//    public static String fetchPRs(String repo, String token) {
//        String url = "https://api.github.com/repos/" + repo + "/pulls?state=open";
//        return callGithub(url, token);
//    }

    public static String fetchPRs(String repo, String token) {
        String url = "https://api.github.com/repos/" + repo + "/pulls?state=open";
        return callGithub(url, token);
    }

    public static String fetchClosedPRs(String repo, String token) {
        String url = "https://api.github.com/repos/" + repo + "/pulls?state=closed&per_page=100";
        return callGithub(url, token);
    }

    public static String fetchIssues(String repo, String token) {
        String url = "https://api.github.com/repos/" + repo + "/issues?state=open";
        return callGithub(url, token);
    }

    public static String fetchBranches(String repo, String token) {
        String url = "https://api.github.com/repos/" + repo + "/branches";
        return callGithub(url, token);
    }

    private static String callGithub(String url, String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.github+json");

        if (token != null && !token.isBlank()) {
            headers.setBearerAuth(token);
        }

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            String message = ex.getResponseBodyAsString();
            throw new ResponseStatusException(
                ex.getStatusCode(),
                "GitHub API request failed: " + message,
                ex
            );
        } catch (Exception ex) {
            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Unable to reach GitHub API",
                ex
            );
        }
    }
}