package com.jdeveloperweb.aiagent.service;

import com.jdeveloperweb.aiagent.agent.OrchestratorAgent;
import com.jdeveloperweb.aiagent.dto.*;
import com.jdeveloperweb.aiagent.model.AgentLog;
import com.jdeveloperweb.aiagent.model.AgentSession;
import com.jdeveloperweb.aiagent.repository.AgentLogRepository;
import com.jdeveloperweb.aiagent.repository.AgentSessionRepository;
import com.jdeveloperweb.aiagent.tool.FileSystemTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.service.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class AgentOrchestratorService {

    private static final Logger log = LoggerFactory.getLogger(AgentOrchestratorService.class);

    private final OrchestratorAgent orchestratorAgent;
    private final FileSystemTool fileSystemTool;
    private final AgentIO agentIO;
    private final SessionStoreService sessionStore;
    private final EpisodicMemoryService episodicMemoryService;
    private final AgentSessionRepository sessionRepository;
    private final AgentLogRepository logRepository;
    private final ObjectMapper objectMapper;
    private final PromptService promptService;

    public AgentOrchestratorService(OrchestratorAgent orchestratorAgent,
                                    FileSystemTool fileSystemTool,
                                    AgentIO agentIO,
                                    SessionStoreService sessionStore,
                                    EpisodicMemoryService episodicMemoryService,
                                    AgentSessionRepository sessionRepository,
                                    AgentLogRepository logRepository,
                                    PromptService promptService) {
        this.orchestratorAgent = orchestratorAgent;
        this.fileSystemTool = fileSystemTool;
        this.agentIO = agentIO;
        this.sessionStore = sessionStore;
        this.episodicMemoryService = episodicMemoryService;
        this.sessionRepository = sessionRepository;
        this.logRepository = logRepository;
        this.promptService = promptService;
        this.objectMapper = new ObjectMapper();
    }

    @Transactional
    public void deleteSession(Long id) {
        logRepository.deleteBySessionId(id);
        sessionRepository.deleteById(id);
    }

    public FullCycleResponse runFullCycle(FullCycleRequest request, SseEmitter emitter) {
        AgentSession dbSession = new AgentSession();
        dbSession.setObjective(request.getObjective());
        dbSession.setStatus("IN_PROGRESS");
        dbSession.setCreatedAt(LocalDateTime.now());
        dbSession.setSpecOnly(request.isSpecOnly());
        dbSession = sessionRepository.save(dbSession);

        Long dbSessionId = dbSession.getId();
        String sessionKey = dbSessionId.toString();

        sessionStore.iniciarContexto(sessionKey, request);
        agentIO.setContext(dbSessionId, emitter);
        agentIO.info("Iniciando Ciclo para: " + request.getObjective());

        try {
            // Fase 1: planejamento — o agente pesquisa e decide o plano autonomamente
            agentIO.reflect("Planejando tarefas de execucao...");
            agentIO.setAgentName("Orchestrator");
            String planPrompt = promptService.loadAndReplace("planner-instruction", Map.of(
                    "sessionId", sessionKey,
                    "objective", request.getObjective(),
                    "specOnly", request.isSpecOnly()
            ));
            agentIO.prompt("Contexto de Planejamento", planPrompt);
            Result<String> planResult = orchestratorAgent.planejar(sessionKey, 
                    dev.langchain4j.data.message.SystemMessage.from(promptService.load("orchestrator-planning")), 
                    planPrompt);
            agentIO.clearAgentName();
            String taskPlan = planResult.content();

            sessionStore.getContext(sessionKey).setAnalysisPlan(taskPlan);
            agentIO.step("Plano de tarefas definido.");

            // Fase 2: execucao orientada a ferramentas
            // O taskPlan ja esta na memoria de chat como resposta do planejar — nao duplicar aqui
            String execContext = promptService.loadAndReplace("execution-instruction", Map.of(
                    "sessionId", sessionKey,
                    "objective", request.getObjective(),
                    "specOnly", request.isSpecOnly()
            ));

            agentIO.setAgentName("Orchestrator");
            agentIO.prompt("Contexto de Execução", execContext);
            orchestratorAgent.executar(sessionKey, 
                    dev.langchain4j.data.message.SystemMessage.from(promptService.load("orchestrator-execution")), 
                    execContext);
            agentIO.clearAgentName();

            // Montagem da resposta a partir do estado acumulado na sessao
            FullCycleResponse response = sessionStore.montarResposta(sessionKey);

            AgentContext context = sessionStore.getContext(sessionKey);
            String doc = context.assembleDocument();

            String savedFilePath = null;
            if (request.isSaveFiles()) {
                String fileName = "doc_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".md";
                savedFilePath = fileSystemTool.salvarArquivo(fileName, doc);
                if (response.getDocument() != null) response.getDocument().setFilePath(savedFilePath);
                agentIO.step("Especificacao salva em: " + fileName);
            }

            episodicMemoryService.store(request.getObjective(), doc, savedFilePath != null ? savedFilePath : "");
            agentIO.step("Especificacao indexada na memoria episodica.");

            dbSession.setStatus("COMPLETE");
            dbSession.setResultJson(objectMapper.writeValueAsString(response));
            sessionRepository.save(dbSession);

            agentIO.step("Ciclo finalizado com sucesso!");
            sendEvent(dbSessionId, emitter, "COMPLETE", "Tudo pronto.", response);
            return response;

        } catch (Exception e) {
            log.error("Erro no ciclo completo: ", e);
            sendEvent(dbSessionId, emitter, "ERROR", e.getMessage());
            return null;
        } finally {
            agentIO.clear();
            sessionStore.clear(sessionKey);
        }
    }

    public java.util.List<AgentLog> getLogsForSession(Long sessionId) {
        return logRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    private void sendEvent(Long dbSessionId, SseEmitter emitter, String type, String message) {
        sendEvent(dbSessionId, emitter, type, message, null);
    }

    private void sendEvent(Long dbSessionId, SseEmitter emitter, String type, String message, Object data) {
        ProgressEvent event = new ProgressEvent(type, message, data);

        AgentLog logEntry = new AgentLog();
        logEntry.setSessionId(dbSessionId);
        logEntry.setType(type);
        logEntry.setMessage(message);
        logEntry.setCreatedAt(LocalDateTime.now());
        logRepository.save(logEntry);

        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("progress").data(event));
            } catch (Exception e) {
                log.warn("Falha ao enviar evento SSE: {}", e.getMessage());
            }
        }
    }
}
