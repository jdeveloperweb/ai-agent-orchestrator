package com.jdeveloperweb.aiagent.dto;

import java.util.List;

public class CodeGenerationResponse {
    private List<GeneratedFile> files;
    private String notes;

    public CodeGenerationResponse() {}

    public CodeGenerationResponse(List<GeneratedFile> files, String notes) {
        this.files = files;
        this.notes = notes;
    }

    public List<GeneratedFile> getFiles() { return files; }
    public void setFiles(List<GeneratedFile> files) { this.files = files; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
