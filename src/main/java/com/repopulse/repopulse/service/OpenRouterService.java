package com.repopulse.repopulse.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.repopulse.repopulse.dto.OpenRouter;
import com.repopulse.repopulse.entity.RepoAnalysis;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Service
public class OpenRouterService {

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    public OpenRouter generateRecommendation(RepoAnalysis analysis) throws Exception {

        String prompt = """
You are a DevOps repository health advisor.

Based on the data below, return ONLY a valid JSON object.

Do not add explanations.
Do not add markdown.
Do not add headings.
Return raw JSON only.

Structure:
{
  "repo": "string",
  "healthScore": number,
  "recommendations": [
    "string",
    "string",
    "string"
  ]
}

Data:
Repo: %s
Total PRs: %d
Stale PRs: %d
Health Score: %d
""".formatted(
                analysis.getRepoName(),
                analysis.getTotalPRs(),
                analysis.getStalePRs(),
                analysis.getHealthScore()
        );

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", 300,
                "temperature", 0.2,
                "response_format", Map.of("type", "json_object")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://openrouter.ai/api/v1/chat/completions",
                HttpMethod.POST,
                entity,
                String.class
        );

        ObjectMapper mapper = new ObjectMapper();
        String rawResponse = response.getBody();
        JsonNode root = mapper.readTree(rawResponse);
        String content = root
                .get("choices")
                .get(0)
                .get("message")
                .get("content")
                .asText();

        try{
            return mapper.readValue(content, OpenRouter.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}