package com.jdeveloperweb.aiagent.agent;

import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface DocumentAgent {

    @SystemMessage({
        "Voce e um Arquiteto de Software Especialista.",
        "Sua missao e criar especificacoes tecnicas detalhadas.",
        "Use um tom profissional, tecnico e focado em implementacao.",
        "Responda sempre em Portugues do Brasil."
    })
    Result<String> generateDocument(@UserMessage String prompt);
}