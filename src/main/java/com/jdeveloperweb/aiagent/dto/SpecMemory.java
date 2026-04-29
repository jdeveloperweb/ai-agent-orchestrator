package com.jdeveloperweb.aiagent.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SpecMemory {
    private String id;
    private String objective;
    private String filePath;
    private String contentPreview;
    private LocalDateTime createdAt;
    private List<String> keywords;

    public SpecMemory() {}

    public SpecMemory(String id, String objective, String filePath,
                      String contentPreview, LocalDateTime createdAt, List<String> keywords) {
        this.id = id;
        this.objective = objective;
        this.filePath = filePath;
        this.contentPreview = contentPreview;
        this.createdAt = createdAt;
        this.keywords = keywords;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getObjective() { return objective; }
    public void setObjective(String objective) { this.objective = objective; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getContentPreview() { return contentPreview; }
    public void setContentPreview(String contentPreview) { this.contentPreview = contentPreview; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }
}
