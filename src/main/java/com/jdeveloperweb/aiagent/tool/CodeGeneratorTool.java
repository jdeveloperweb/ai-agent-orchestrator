package com.jdeveloperweb.aiagent.tool;

import com.jdeveloperweb.aiagent.agent.CodeAgent;
import com.jdeveloperweb.aiagent.dto.AgentContext;
import com.jdeveloperweb.aiagent.dto.AgentTask;
import com.jdeveloperweb.aiagent.service.AgentIO;
import com.jdeveloperweb.aiagent.service.SessionStoreService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.Result;
import org.springframework.stereotype.Component;

@Component
public class CodeGeneratorTool {

    private final CodeAgent codeAgent;
    private final SessionStoreService sessionStore;
    private final AgentIO agentIO;

    public CodeGeneratorTool(CodeAgent codeAgent, SessionStoreService sessionStore, AgentIO agentIO) {
        this.codeAgent = codeAgent;
        this.sessionStore = sessionStore;
        this.agentIO = agentIO;
    }

    @Tool("Gera codigo-fonte baseado na especificacao tecnica aprovada. " +
         "Chame SOMENTE quando a especificacao estiver aprovada pelo Critico e spec_only for false.")
    public String gerarCodigo(String sessionId) {
        AgentContext context = sessionStore.getContext(sessionId);
        String document = context.assembleDocument();

        agentIO.setAgentName("Code Architect");
        String codePrompt = "Objetivo: " + context.getObjective() + "\n\nEspecificacao:\n" + document;
        agentIO.prompt("Instrucoes de Geracao de Codigo", codePrompt);

        Result<String> result = codeAgent.generateCode(codePrompt);
        agentIO.clearAgentName();

        if (result.tokenUsage() != null) context.addTokenUsage(result.tokenUsage());
        sessionStore.salvarCodigo(sessionId, result.content());

        agentIO.step("Codigo gerado com sucesso.");
        
        // Auto-marcar no plano de execucao
        AgentTask task = sessionStore.marcarTarefaConcluidaPorBusca(sessionId, "Codigo");
        if (task != null) agentIO.taskDone(task.getNumero(), task.getDescricao());

        return "Codigo gerado e armazenado na sessao.";
    }
}
