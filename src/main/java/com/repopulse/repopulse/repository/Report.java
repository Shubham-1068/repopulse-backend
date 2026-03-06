package com.repopulse.repopulse.repository;

import com.repopulse.repopulse.entity.RepoAnalysis;
import com.repopulse.repopulse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface Report extends JpaRepository<RepoAnalysis, Long> {
    List<RepoAnalysis> findByRepoNameOrderByAnalyzedAtDesc(String repoName);
    List<RepoAnalysis> findByUserOrderByAnalyzedAtDesc(User user);
}
