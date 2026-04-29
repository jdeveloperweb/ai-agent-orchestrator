package com.jdeveloperweb.aiagent.service;

import com.jdeveloperweb.aiagent.dto.AgentContext;
import com.jdeveloperweb.aiagent.dto.AgentTask;
import com.jdeveloperweb.aiagent.dto.ProgressEvent;
import com.jdeveloperweb.aiagent.model.AgentLog;
import com.jdeveloperweb.aiagent.repository.AgentLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class AgentIO {
    private static final Logger log = LoggerFactory.getLogger(AgentIO.class);
    private final AgentLogRepository logRepository;
    private final SessionStoreService sessionStore;
    private static final ThreadLocal<SseEmitter> currentEmitter = new ThreadLocal<>();
    private static final ThreadLocal<Long> currentSessionId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentAgentName = new ThreadLocal<>();

    public AgentIO(AgentLogRepository logRepository, @Lazy SessionStoreService sessionStore) {
        this.logRepository = logRepository;
        this.sessionStore = sessionStore;
    }

    public void setContext(Long sessionId, SseEmitter emitter) {
        currentSessionId.set(sessionId);
        currentEmitter.set(emitter);
    }

    public void setAgentName(String name) {
        currentAgentName.set(name);
    }

    public void clearAgentName() {
        currentAgentName.remove();
    }

    public void clear() {
        currentEmitter.remove();
        currentSessionId.remove();
        currentAgentName.remove();
    }

    public void persistRagInsight(String source, String query, String result) {
        Long sessionId = currentSessionId.get();
        if (sessionId == null || result == null || result.isBlank()) return;
        String sessionKey = sessionId.toString();
        AgentContext ctx = sessionStore.getContext(sessionKey);
        if (ctx == null) return;
        String truncated = result.length() > 500 ? result.substring(0, 500) + "..." : result;
        ctx.addRagInsight("[" + source + " | " + query + "]: " + truncated);
    }

    public void info(String message) {
        send("THOUGHT", message);
    }

    public void warn(String message) {
        send("WARNING", message);
    }

    public void error(String message) {
        send("ERROR", message);
    }

    public void toolStart(String message) {
        send("TOOL_START", message);
    }

    public void toolStart(String message, Object data) {
        send("TOOL_START", message, data);
    }

    public void toolEnd(String message) {
        send("TOOL_END", message);
    }

    public void toolEnd(String message, Object data) {
        send("TOOL_END", message, data);
    }

    public void step(String message) {
        send("STEP_COMPLETE", message);
    }

    public void fileCreated(String path) {
        send("FILE_CREATED", path);
    }

    public void prompt(String message) {
        send("PROMPT", message);
    }

    public void prompt(String title, String content) {
        send("PROMPT", title, content);
    }

    public void reflect(String message) {
        send("REFLECTION", message);
    }

    public void taskList(List<AgentTask> tarefas) {
        send("TASK_LIST", "Tarefas registradas: " + tarefas.size(), tarefas);
    }

    public void taskDone(int numero, String descricao) {
        send("TASK_DONE", numero + ". " + descricao, Map.of("numero", numero));
    }

    public void usage(int input, int output, int total) {
        send("USAGE", "Tokens: " + total, Map.of(
            "input", input,
            "output", output,
            "total", total
        ));
    }

    private void send(String type, String message) {
        send(type, message, null);
    }

    private void send(String type, String message, Object data) {
        SseEmitter emitter = currentEmitter.get();
        Long sessionId = currentSessionId.get();
        String agentName = currentAgentName.get();

        // 1. Salvar no Banco de Dados (Persistência para o Histórico)
        if (sessionId != null) {
            try {
                AgentLog logEntry = new AgentLog();
                logEntry.setSessionId(sessionId);
                logEntry.setType(type);
                logEntry.setMessage(message + (agentName != null ? " [" + agentName + "]" : ""));
                logEntry.setCreatedAt(LocalDateTime.now());
                logRepository.save(logEntry);
            } catch (Exception e) {
                log.warn("Falha ao persistir log: {}", e.getMessage());
            }
        }

        // 2. Enviar via SSE (Tempo Real)
        if (emitter != null) {
            try {
                ProgressEvent event = new ProgressEvent(type, message, agentName, data);
                emitter.send(SseEmitter.event().name("progress").data(event));
            } catch (Exception e) {
                log.warn("Falha ao enviar evento IO: {}", e.getMessage());
            }
        }
        log.info("[{}] {}{}", type, message, agentName != null ? " (" + agentName + ")" : "");
    }
}
