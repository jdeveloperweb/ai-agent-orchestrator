package com.jdeveloperweb.aiagent.dto;

public class RagChatRequest {
    private String message;
    private String provider = "OPENAI";
    private String tenantId = "default";
    private Boolean useSpringAi = true;

    public RagChatRequest() {}

    public RagChatRequest(String message) {
        this.message = message;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public Boolean getUseSpringAi() { return useSpringAi; }
    public void setUseSpringAi(Boolean useSpringAi) { this.useSpringAi = useSpringAi; }
}
