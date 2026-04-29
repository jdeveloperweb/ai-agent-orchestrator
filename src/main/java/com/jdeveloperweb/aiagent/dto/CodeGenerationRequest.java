package com.jdeveloperweb.aiagent.dto;

public class CodeGenerationRequest {
    private String document;
    private String targetStack;
    private String outputInstruction;
    private boolean saveFiles;

    public CodeGenerationRequest() {}

    public CodeGenerationRequest(String document, String targetStack, String outputInstruction, boolean saveFiles) {
        this.document = document;
        this.targetStack = targetStack;
        this.outputInstruction = outputInstruction;
        this.saveFiles = saveFiles;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getDocument() { return document; }
    public void setDocument(String document) { this.document = document; }
    public String getTargetStack() { return targetStack; }
    public void setTargetStack(String targetStack) { this.targetStack = targetStack; }
    public String getOutputInstruction() { return outputInstruction; }
    public void setOutputInstruction(String outputInstruction) { this.outputInstruction = outputInstruction; }
    public boolean isSaveFiles() { return saveFiles; }
    public void setSaveFiles(boolean saveFiles) { this.saveFiles = saveFiles; }

    public static class Builder {
        private String document;
        private String targetStack;
        private String outputInstruction;
        private boolean saveFiles;

        public Builder document(String document) { this.document = document; return this; }
        public Builder targetStack(String targetStack) { this.targetStack = targetStack; return this; }
        public Builder outputInstruction(String outputInstruction) { this.outputInstruction = outputInstruction; return this; }
        public Builder saveFiles(boolean saveFiles) { this.saveFiles = saveFiles; return this; }
        public CodeGenerationRequest build() {
            return new CodeGenerationRequest(document, targetStack, outputInstruction, saveFiles);
        }
    }
}
