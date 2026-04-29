package com.jdeveloperweb.aiagent.tool;

import com.jdeveloperweb.aiagent.agent.CriticAgent;
import com.jdeveloperweb.aiagent.dto.AgentContext;
import com.jdeveloperweb.aiagent.dto.AgentTask;
import com.jdeveloperweb.aiagent.service.AgentIO;
import com.jdeveloperweb.aiagent.service.SessionStoreService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.Result;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CriticTool {

    private static final int MAX_REVIEW_CYCLES = 3;
    private static final Pattern REESCREVER_PATTERN =
            Pattern.compile("REESCREVER_SECOES:\\s*(.+)", Pattern.CASE_INSENSITIVE);

    private final CriticAgent criticAgent;
    private final SessionStoreService sessionStore;
    private final AgentIO agentIO;
    private final PromptService promptService;

    public CriticTool(CriticAgent criticAgent, 
                      SessionStoreService sessionStore, 
                      AgentIO agentIO,
                      PromptService promptService) {
        this.criticAgent = criticAgent;
        this.sessionStore = sessionStore;
        this.agentIO = agentIO;
        this.promptService = promptService;
    }

    @Tool("Revisa o documento gerado ate o momento, verificando qualidade e completude conforme as regras IFPUG. " +
         "Retorna 'APROVADO' ou instrucoes explicitas de quais secoes reescrever.")
    public String revisarDocumento(String sessionId) {
        AgentContext context = sessionStore.getContext(sessionId);
        String document = context.assembleDocument();

        agentIO.setAgentName("System Critic");
        agentIO.prompt("Conteudo para Revisao", document);

        Result<String> result = criticAgent.reviewDocument(
                dev.langchain4j.data.message.SystemMessage.from(promptService.load("critic-agent")),
                document);
        agentIO.clearAgentName();

        if (result.tokenUsage() != null) context.addTokenUsage(result.tokenUsage());

        String review = result.content().trim();
        int iteration = context.getDocumentIteration() + 1;
        context.setDocumentIteration(iteration);

        boolean aprovado = review.toUpperCase().startsWith("APROVADO");

        if (aprovado) {
            agentIO.step("Revisao (Iteracao " + iteration + "): APROVADO");
            AgentTask task = sessionStore.marcarTarefaConcluidaPorBusca(sessionId, "Revisar");
            if (task != null) agentIO.taskDone(task.getNumero(), task.getDescricao());
            return "APROVADO";
        }

        // Limite de ciclos atingido: nao forcar aprovacao, mas encerrar o loop de reescrita
        if (iteration >= MAX_REVIEW_CYCLES) {
            agentIO.warn("Limite de " + MAX_REVIEW_CYCLES + " ciclos de revisao atingido. Pendencias remanescentes registradas.");
            context.addReflectionFeedback("[Ciclo " + iteration + " — limite atingido] " + review);
            AgentTask task = sessionStore.marcarTarefaConcluidaPorBusca(sessionId, "Revisar");
            if (task != null) agentIO.taskDone(task.getNumero(), task.getDescricao());
            return "LIMITE_REVISOES_ATINGIDO: Prossiga para avaliacao de confianca. Pendencias registradas no historico de reflexao.";
        }

        // Parsear quais secoes precisam ser reescritas
        List<String> secoesParaReescrever = parseSections(review);
        context.addReflectionFeedback("[Ciclo " + iteration + "] " + review);

        agentIO.warn("Revisao (Iteracao " + iteration + "): Pendencias em " + secoesParaReescrever);

        // Retornar instrucao estruturada e explicita para o Orchestrator
        StringBuilder instrucao = new StringBuilder();
        instrucao.append("PENDENCIAS_ENCONTRADAS\n");
        instrucao.append(review).append("\n\n");
        instrucao.append("PROXIMOS_PASSOS (execute nesta ordem exata):\n");
        for (int i = 0; i < secoesParaReescrever.size(); i++) {
            instrucao.append(i + 1).append(". Chame escreverSecao(sessionId=\"")
                     .append(sessionId).append("\", secaoId=\"")
                     .append(secoesParaReescrever.get(i)).append("\")\n");
        }
        instrucao.append(secoesParaReescrever.size() + 1)
                 .append(". Chame revisarDocumento(sessionId=\"").append(sessionId).append("\") novamente\n");

        return instrucao.toString();
    }

    private List<String> parseSections(String review) {
        List<String> sections = new ArrayList<>();
        Matcher m = REESCREVER_PATTERN.matcher(review);
        if (m.find()) {
            String[] parts = m.group(1).split("[,;]");
            for (String part : parts) {
                String trimmed = part.trim().replaceAll("[\\[\\]]", "");
                if (!trimmed.isBlank()) sections.add(trimmed);
            }
        }
        // Fallback: se o Critico nao usou o formato correto, detecta secoes b.N e c) no texto
        if (sections.isEmpty()) {
            Matcher fallback = Pattern.compile("\\b([bc](?:\\.\\d+)?)\\)", Pattern.CASE_INSENSITIVE).matcher(review);
            while (fallback.find()) {
                String sec = fallback.group(0);
                if (!sections.contains(sec)) sections.add(sec);
            }
        }
        return sections;
    }
}
