package com.repopulse.repopulse.dto;

public class AnalysisRequest {

    private String repoName;
    private String token;

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}