package com.repopulse.repopulse.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class RepoAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String repoName;
    private int totalPRs;
    private int closedPRs;
    private int mergedPRs;
    private int unmergedClosedPRs;
    private int stalePRs;
    private int inactiveIssues;
    private int deadBranches;
    private int healthScore;
    private String aiRecommendations;
    private LocalDateTime analyzedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
