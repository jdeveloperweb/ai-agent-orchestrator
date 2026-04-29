package com.jdeveloperweb.aiagent.dto;

import dev.langchain4j.model.output.TokenUsage;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AgentContext {

    private String sessionId;
    private final String objective;
    private String targetStack;
    private String analysisPlan;
    private String generatedDocument;
    private int documentIteration;
    private final List<String> ragInsights = Collections.synchronizedList(new ArrayList<>());
    private final List<String> reflectionHistory = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, String> sectionResults = Collections.synchronizedMap(new LinkedHashMap<>());
    
    private final java.util.concurrent.atomic.AtomicInteger inputTokens = new java.util.concurrent.atomic.AtomicInteger(0);
    private final java.util.concurrent.atomic.AtomicInteger outputTokens = new java.util.concurrent.atomic.AtomicInteger(0);

    public AgentContext(String objective) {
        this.objective = objective;
    }

    public AgentContext(String sessionId, String objective, String targetStack) {
        this.sessionId = sessionId;
        this.objective = objective;
        this.targetStack = targetStack;
    }

    public void addTokenUsage(TokenUsage usage) {
        if (usage == null) return;
        if (usage.inputTokenCount() != null) this.inputTokens.addAndGet(usage.inputTokenCount());
        if (usage.outputTokenCount() != null) this.outputTokens.addAndGet(usage.outputTokenCount());
    }

    public int getInputTokens() { return inputTokens.get(); }
    public int getOutputTokens() { return outputTokens.get(); }
    public int getTotalTokens() { return inputTokens.get() + outputTokens.get(); }

    public void addRagInsight(String insight) {
        ragInsights.add(insight);
    }

    public void addReflectionFeedback(String feedback) {
        reflectionHistory.add("Revisao " + (documentIteration + 1) + ": " + feedback);
    }

    public void putSection(String sectionKey, String content) {
        sectionResults.put(sectionKey, content);
    }

    public String assembleDocument() {
        StringBuilder sb = new StringBuilder();
        boolean bHeaderAdded = false;
        for (Map.Entry<String, String> entry : sectionResults.entrySet()) {
            String key = entry.getKey().toLowerCase().trim();
            if (!bHeaderAdded && key.startsWith("b.")) {
                sb.append("## b) IMPLEMENTAÇÕES:\n\n");
                bHeaderAdded = true;
            }
            sb.append(entry.getValue()).append("\n\n");
        }
        return sb.toString().trim();
    }

    public List<String> getBSectionEntries() {
        return sectionResults.keySet().stream()
                .filter(k -> k.toLowerCase().trim().startsWith("b."))
                .collect(Collectors.toList());
    }

    public String getSessionId() { return sessionId; }
    public String getObjective() { return objective; }
    public String getTargetStack() { return targetStack; }
    public String getAnalysisPlan() { return analysisPlan; }
    public void setAnalysisPlan(String analysisPlan) { this.analysisPlan = analysisPlan; }
    public String getGeneratedDocument() { return generatedDocument; }
    public void setGeneratedDocument(String generatedDocument) { this.generatedDocument = generatedDocument; }
    public int getDocumentIteration() { return documentIteration; }
    public void setDocumentIteration(int documentIteration) { this.documentIteration = documentIteration; }
    public List<String> getRagInsights() { return Collections.unmodifiableList(ragInsights); }
    public List<String> getReflectionHistory() { return Collections.unmodifiableList(reflectionHistory); }
    public Map<String, String> getSectionResults() { return Collections.unmodifiableMap(sectionResults); }
}