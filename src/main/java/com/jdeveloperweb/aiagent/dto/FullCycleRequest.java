package com.jdeveloperweb.aiagent.dto;
 
import com.fasterxml.jackson.annotation.JsonProperty;

public class FullCycleRequest {
    private String sessionId;
    private String objective;
    private String contextQuestion;
    private String targetStack;
    private boolean saveFiles;
    private boolean specOnly; // Nova opção

    public FullCycleRequest() {}

    public FullCycleRequest(String objective, String contextQuestion, String targetStack, boolean saveFiles) {
        this.objective = objective;
        this.contextQuestion = contextQuestion;
        this.targetStack = targetStack;
        this.saveFiles = saveFiles;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getObjective() { return objective; }
    public void setObjective(String objective) { this.objective = objective; }
    public String getContextQuestion() { return contextQuestion; }
    public void setContextQuestion(String contextQuestion) { this.contextQuestion = contextQuestion; }
    public String getTargetStack() { return targetStack; }
    public void setTargetStack(String targetStack) { this.targetStack = targetStack; }
    public boolean isSaveFiles() { return saveFiles; }
    public void setSaveFiles(boolean saveFiles) { this.saveFiles = saveFiles; }

    @JsonProperty("isSpecOnly")
    public boolean isSpecOnly() { return specOnly; }
    @JsonProperty("isSpecOnly")
    public void setSpecOnly(boolean specOnly) { this.specOnly = specOnly; }
}
