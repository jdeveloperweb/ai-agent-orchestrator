package com.jdeveloperweb.aiagent.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "agent_sessions")
public class AgentSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String objective;
    private String status;
    private LocalDateTime createdAt;
    @Column(name = "is_spec_only")
    private boolean specOnly;
    @Column(columnDefinition = "TEXT")
    private String resultJson;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getObjective() { return objective; }
    public void setObjective(String objective) { this.objective = objective; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    @JsonProperty("isSpecOnly")
    public boolean isSpecOnly() { return specOnly; }
    
    @JsonProperty("isSpecOnly")
    public void setSpecOnly(boolean specOnly) { this.specOnly = specOnly; }
    public String getResultJson() { return resultJson; }
    public void setResultJson(String resultJson) { this.resultJson = resultJson; }
}