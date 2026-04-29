package com.jdeveloperweb.aiagent.dto;

public class FullCycleResponse {
    private DocumentResponse document;
    private CodeGenerationResponse code;
    private int documentIterations;
    private boolean specOnly;
    private ConfidenceReport confidenceReport;

    public FullCycleResponse() {}

    public DocumentResponse getDocument() { return document; }
    public void setDocument(DocumentResponse document) { this.document = document; }
    public CodeGenerationResponse getCode() { return code; }
    public void setCode(CodeGenerationResponse code) { this.code = code; }
    public int getDocumentIterations() { return documentIterations; }
    public void setDocumentIterations(int documentIterations) { this.documentIterations = documentIterations; }
    public boolean isSpecOnly() { return specOnly; }
    public void setSpecOnly(boolean specOnly) { this.specOnly = specOnly; }
    public ConfidenceReport getConfidenceReport() { return confidenceReport; }
    public void setConfidenceReport(ConfidenceReport confidenceReport) { this.confidenceReport = confidenceReport; }
}
