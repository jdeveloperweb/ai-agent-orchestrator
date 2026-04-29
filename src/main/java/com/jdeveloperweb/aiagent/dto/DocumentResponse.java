package com.jdeveloperweb.aiagent.dto;

public class DocumentResponse {
    private String content;
    private String filePath;

    public DocumentResponse() {}

    public DocumentResponse(String content, String filePath) {
        this.content = content;
        this.filePath = filePath;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public static class Builder {
        private String content;
        private String filePath;

        public Builder content(String content) { this.content = content; return this; }
        public Builder filePath(String filePath) { this.filePath = filePath; return this; }
        public DocumentResponse build() {
            return new DocumentResponse(content, filePath);
        }
    }
}
