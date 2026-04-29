package com.jdeveloperweb.aiagent.dto;

public class DocumentRequest {
    private String objective;
    private String contextQuestion;
    private boolean saveToFile;

    public DocumentRequest() {}

    public DocumentRequest(String objective, String contextQuestion, boolean saveToFile) {
        this.objective = objective;
        this.contextQuestion = contextQuestion;
        this.saveToFile = saveToFile;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getObjective() { return objective; }
    public void setObjective(String objective) { this.objective = objective; }
    public String getContextQuestion() { return contextQuestion; }
    public void setContextQuestion(String contextQuestion) { this.contextQuestion = contextQuestion; }
    public boolean isSaveToFile() { return saveToFile; }
    public void setSaveToFile(boolean saveToFile) { this.saveToFile = saveToFile; }

    public static class Builder {
        private String objective;
        private String contextQuestion;
        private boolean saveToFile;

        public Builder objective(String objective) { this.objective = objective; return this; }
        public Builder contextQuestion(String contextQuestion) { this.contextQuestion = contextQuestion; return this; }
        public Builder saveToFile(boolean saveToFile) { this.saveToFile = saveToFile; return this; }
        public DocumentRequest build() {
            return new DocumentRequest(objective, contextQuestion, saveToFile);
        }
    }
}
