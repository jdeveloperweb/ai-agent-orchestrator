package com.jdeveloperweb.aiagent.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfidenceReport {
    private String overallConfidence;
    private Map<String, String> sectionConfidence = new LinkedHashMap<>();
    private List<String> attentionPoints = new ArrayList<>();
    private String rawReport;

    public ConfidenceReport() {}

    public String getOverallConfidence() { return overallConfidence; }
    public void setOverallConfidence(String overallConfidence) { this.overallConfidence = overallConfidence; }
    public Map<String, String> getSectionConfidence() { return sectionConfidence; }
    public void setSectionConfidence(Map<String, String> sectionConfidence) { this.sectionConfidence = sectionConfidence; }
    public List<String> getAttentionPoints() { return attentionPoints; }
    public void setAttentionPoints(List<String> attentionPoints) { this.attentionPoints = attentionPoints; }
    public String getRawReport() { return rawReport; }
    public void setRawReport(String rawReport) { this.rawReport = rawReport; }
}
