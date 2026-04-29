package com.jdeveloperweb.aiagent.agent;

import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface CodeAgent {
    @SystemMessage({
        "Gere a implementacao de codigo baseada na especificacao tecnica fornecida.",
        "Siga os padroes de projeto solicitados."
    })
    Result<String> generateCode(@UserMessage String prompt);
}