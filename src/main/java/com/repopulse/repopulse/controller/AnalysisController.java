package com.repopulse.repopulse.controller;

import com.repopulse.repopulse.dto.AnalysisRequest;
import com.repopulse.repopulse.entity.RepoAnalysis;
import com.repopulse.repopulse.service.AnalysisService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/analyze")
@CrossOrigin(origins = "*")
public class AnalysisController {
    private final AnalysisService service;

    public AnalysisController(AnalysisService service) {
        this.service=service;
    }

    @PostMapping
    public RepoAnalysis analyzeRepo(
            @RequestBody AnalysisRequest req,
            @RequestHeader("Authorization") String authHeader
    ) throws Exception {

        return service.analyze(req, authHeader);
    }

    @GetMapping("/history")
    public List<RepoAnalysis> getHistory(
            @RequestHeader("Authorization") String authHeader
    ) throws Exception {

        return service.getUserHistory(authHeader);
    }
}
