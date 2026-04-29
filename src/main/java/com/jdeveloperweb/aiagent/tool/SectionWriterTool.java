package com.jdeveloperweb.aiagent.tool;

import com.jdeveloperweb.aiagent.agent.SectionAgent;
import com.jdeveloperweb.aiagent.dto.AgentContext;
import com.jdeveloperweb.aiagent.dto.AgentTask;
import com.jdeveloperweb.aiagent.service.AgentIO;
import com.jdeveloperweb.aiagent.service.SessionStoreService;
import com.jdeveloperweb.aiagent.service.TemplateService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.Result;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SectionWriterTool {

    private final SessionStoreService sessionStore;
    private final TemplateService templateService;
    private final AgentIO agentIO;
    private final PromptService promptService;

    public SectionWriterTool(SectionAgent sectionAgent,
                              SessionStoreService sessionStore,
                              TemplateService templateService, 
                              AgentIO agentIO,
                              PromptService promptService) {
        this.sectionAgent = sectionAgent;
        this.sessionStore = sessionStore;
        this.templateService = templateService;
        this.agentIO = agentIO;
        this.promptService = promptService;
    }

    @Tool("Escreve ou reescreve uma secao da especificacao tecnica. " +
         "Use o identificador exato da secao (ex: 'a) CONTEXTO', 'b.1) Nome da Funcao'). " +
         "A revisao de qualidade e feita pelo 'revisarDocumento' apos todas as secoes serem escritas.")
    public String escreverSecao(String sessionId, String secaoId) {
        AgentContext context = sessionStore.getContext(sessionId);
        agentIO.setAgentName("Section Analyst");
        agentIO.reflect("Gerando secao: " + secaoId);

        String template = templateService.getTemplate("full_cycle_plan");
        String sectionTemplate = extractSectionTemplate(secaoId, template);
        String sectionPrompt = promptService.buildSectionPrompt(context, secaoId, sectionTemplate);
        if (secaoId.toLowerCase().trim().startsWith("c)")) {
            sectionPrompt += buildPfValidationBlock(context);
        }

        Result<String> result = sectionAgent.generateSection(
                dev.langchain4j.data.message.SystemMessage.from(promptService.load("section-agent")),
                sectionPrompt);
        String content = result.content();
        if (result.tokenUsage() != null) context.addTokenUsage(result.tokenUsage());

        agentIO.clearAgentName();
        context.putSection(secaoId, content);
        agentIO.step("Secao concluida: " + secaoId);

        AgentTask task = sessionStore.marcarTarefaConcluidaPorBusca(sessionId, secaoId);
        if (task != null) agentIO.taskDone(task.getNumero(), task.getDescricao());

        return "Secao '" + secaoId + "' gerada com sucesso.";
    }

    private String buildPfValidationBlock(AgentContext context) {
        List<String> bSections = context.getBSectionEntries();
        if (bSections.isEmpty()) {
            return "\n\n=== VALIDACAO PONTOS DE FUNCAO ===\n" +
                   "Nenhuma secao b.N encontrada ainda. A tabela deve ter 0 linhas (improvavel — verifique o documento).\n";
        }
        String lista = bSections.stream()
                .map(s -> "  - " + s)
                .collect(Collectors.joining("\n"));
        return "\n\n=== VALIDACAO OBRIGATORIA — PONTOS DE FUNCAO ===\n" +
               "Este documento possui EXATAMENTE " + bSections.size() + " secao(oes) b.N:\n" +
               lista + "\n" +
               "A tabela IFPUG DEVE ter EXATAMENTE " + bSections.size() + " linha(s) — nem mais, nem menos.\n" +
               "Cada linha corresponde a UMA secao b.N acima, na mesma ordem.\n";
    }

    private String extractSectionTemplate(String sectionKey, String fullTemplate) {
        String lowerKey = sectionKey.toLowerCase();
        String prefix = lowerKey.substring(0, Math.min(5, lowerKey.length()));
        String[] lines = fullTemplate.split("\n");
        StringBuilder sb = new StringBuilder();
        boolean capturing = false;
        for (String line : lines) {
            String lower = line.toLowerCase();
            if (!capturing && lower.contains(prefix)) {
                capturing = true;
            } else if (capturing && line.startsWith("### ") && !lower.contains(prefix)) {
                break;
            }
            if (capturing) sb.append(line).append("\n");
        }
        return sb.length() > 0 ? sb.toString() : "Formato padrao para: " + sectionKey;
    }
}
