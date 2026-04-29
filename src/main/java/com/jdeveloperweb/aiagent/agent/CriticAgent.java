package com.jdeveloperweb.aiagent.agent;

import dev.langchain4j.service.Result;
import dev.langchain4j.service.UserMessage;

public interface CriticAgent {
    Result<String> reviewDocument(dev.langchain4j.data.message.SystemMessage systemMessage, @UserMessage String document);
}