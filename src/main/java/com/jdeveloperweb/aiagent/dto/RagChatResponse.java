package com.jdeveloperweb.aiagent.dto;

public class RagChatResponse {
    private String answer;

    public RagChatResponse() {}

    public RagChatResponse(String answer) {
        this.answer = answer;
    }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
}
