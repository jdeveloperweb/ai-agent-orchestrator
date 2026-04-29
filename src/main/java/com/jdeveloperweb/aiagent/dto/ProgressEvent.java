package com.jdeveloperweb.aiagent.dto;

public class ProgressEvent {
    private String type; // THOUGHT, TOOL_START, TOOL_END, STEP_COMPLETE, ERROR, REFLECTION
    private String message;
    private String agentName;
    private Object data;

    public ProgressEvent() {}

    public ProgressEvent(String type, String message) {
        this.type = type;
        this.message = message;
    }

    public ProgressEvent(String type, String message, String agentName, Object data) {
        this.type = type;
        this.message = message;
        this.agentName = agentName;
        this.data = data;
    }

    public ProgressEvent(String type, String message, Object data) {
        this.type = type;
        this.message = message;
        this.data = data;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}
