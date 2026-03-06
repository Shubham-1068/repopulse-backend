package com.repopulse.repopulse.dto;

import lombok.Data;
import java.util.List;

@Data
public class OpenRouter {
    private String repo;
    private int healthScore;
    private List<String> recommendations;
}