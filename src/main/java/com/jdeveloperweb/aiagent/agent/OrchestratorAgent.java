package com.jdeveloperweb.aiagent.agent;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface OrchestratorAgent {

    Result<String> planejar(@MemoryId String sessionId, dev.langchain4j.data.message.SystemMessage systemMessage, @UserMessage String objetivo);

    Result<String> executar(@MemoryId String sessionId, dev.langchain4j.data.message.SystemMessage systemMessage, @UserMessage String contexto);
}
