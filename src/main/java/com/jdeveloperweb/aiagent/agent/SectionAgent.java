package com.jdeveloperweb.aiagent.agent;

import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface SectionAgent {
    Result<String> generateSection(dev.langchain4j.data.message.SystemMessage systemMessage, @UserMessage String sectionPrompt);
}