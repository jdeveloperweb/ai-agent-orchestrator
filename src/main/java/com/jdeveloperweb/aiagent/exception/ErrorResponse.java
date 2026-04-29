package com.jdeveloperweb.aiagent.exception;

import java.time.LocalDateTime;

public class ErrorResponse {
    private final String message;
    private final LocalDateTime timestamp;

    public ErrorResponse(String message) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
