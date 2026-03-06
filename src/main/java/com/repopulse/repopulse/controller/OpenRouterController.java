package com.repopulse.repopulse.controller;

import com.repopulse.repopulse.dto.AnalysisRequest;
import com.repopulse.repopulse.dto.OpenRouter;
import com.repopulse.repopulse.entity.RepoAnalysis;
import com.repopulse.repopulse.repository.Report;
import com.repopulse.repopulse.service.AnalysisService;
import com.repopulse.repopulse.service.OpenRouterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class OpenRouterController {

    private final Report repository;
    private final OpenRouterService aiService;

    public OpenRouterController(Report repository, OpenRouterService aiService) {
        this.repository = repository;
        this.aiService = aiService;
    }

    @PostMapping("/{id}")
    public OpenRouter generate(@PathVariable Long id) throws Exception {

        RepoAnalysis analysis = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

        return aiService.generateRecommendation(analysis);
    }
}
