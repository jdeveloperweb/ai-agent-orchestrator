package com.jdeveloperweb.aiagent.tool;

import com.jdeveloperweb.aiagent.agent.UncertaintyAgent;
import com.jdeveloperweb.aiagent.dto.AgentContext;
import com.jdeveloperweb.aiagent.dto.AgentTask;
import com.jdeveloperweb.aiagent.dto.ConfidenceReport;
import com.jdeveloperweb.aiagent.service.AgentIO;
import com.jdeveloperweb.aiagent.service.SessionStoreService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.Result;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ConfidenceTool {

    private final AgentIO agentIO;
    private final PromptService promptService;

    public ConfidenceTool(UncertaintyAgent uncertaintyAgent, 
                          SessionStoreService sessionStore, 
                          AgentIO agentIO,
                          PromptService promptService) {
        this.uncertaintyAgent = uncertaintyAgent;
        this.sessionStore = sessionStore;
        this.agentIO = agentIO;
        this.promptService = promptService;
    }

    @Tool("Avalia o nivel de confianca e completude da especificacao tecnica gerada. " +
         "Chame apos a aprovacao do Critico para gerar o relatorio de confianca por secao.")
    public String avaliarConfianca(String sessionId) {
        AgentContext context = sessionStore.getContext(sessionId);
        String document = context.assembleDocument();

        agentIO.setAgentName("Uncertainty Analyst");
        Result<String> result = uncertaintyAgent.evaluate(
                dev.langchain4j.data.message.SystemMessage.from(promptService.load("uncertainty-agent")),
                document);
        agentIO.clearAgentName();

        if (result.tokenUsage() != null) context.addTokenUsage(result.tokenUsage());

        ConfidenceReport report = parseConfidenceReport(result.content());
        sessionStore.salvarConfianca(sessionId, report);

        agentIO.step("Confianca avaliada.");
        
        // Auto-marcar no plano de execucao
        AgentTask task = sessionStore.marcarTarefaConcluidaPorBusca(sessionId, "Confianca");
        if (task != null) agentIO.taskDone(task.getNumero(), task.getDescricao());

        return result.content();
    }

    private ConfidenceReport parseConfidenceReport(String raw) {
        ConfidenceReport report = new ConfidenceReport();
        for (String line : raw.split("\n")) {
            if (line.contains("CONFIANCA_GERAL:")) {
                report.setOverallConfidence(extractValue(line));
            } else if (line.startsWith("SECAO_")) {
                String key = line.substring(0, line.indexOf(":")).replace("SECAO_", "").toLowerCase();
                report.getSectionConfidence().put(key, extractValue(line));
            } else if (line.startsWith("PONTOS_ATENCAO:")) {
                report.setAttentionPoints(Arrays.asList(extractValue(line).split(";")));
            }
        }
        return report;
    }

    private String extractValue(String line) {
        int idx = line.indexOf(":");
        return idx >= 0 ? line.substring(idx + 1).trim() : line.trim();
    }
}
