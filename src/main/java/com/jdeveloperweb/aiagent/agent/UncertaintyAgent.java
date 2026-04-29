package com.jdeveloperweb.aiagent.agent;

import dev.langchain4j.service.Result;
import dev.langchain4j.service.UserMessage;

public interface UncertaintyAgent {
    Result<String> evaluate(dev.langchain4j.data.message.SystemMessage systemMessage, @UserMessage String document);
}