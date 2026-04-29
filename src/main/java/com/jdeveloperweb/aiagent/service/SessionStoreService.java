package com.jdeveloperweb.aiagent.service;

import com.jdeveloperweb.aiagent.dto.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionStoreService {

    private final Map<String, AgentContext> contexts = new ConcurrentHashMap<>();
    private final Map<String, List<ProgressEvent>> logHistory = new ConcurrentHashMap<>();
    private final Map<String, List<AgentTask>> taskLists = new ConcurrentHashMap<>();
    private final Map<String, ConfidenceReport> confidenceReports = new ConcurrentHashMap<>();
    private final Map<String, String> codeResults = new ConcurrentHashMap<>();
    private final Map<String, FullCycleRequest> requests = new ConcurrentHashMap<>();

    public void iniciarContexto(String sessionId, FullCycleRequest request) {
        AgentContext context = new AgentContext(sessionId, request.getObjective(), request.getTargetStack());
        contexts.put(sessionId, context);
        requests.put(sessionId, request);
    }

    public void saveContext(AgentContext context) {
        contexts.put(context.getSessionId(), context);
    }

    public AgentContext getContext(String sessionId) {
        return contexts.get(sessionId);
    }

    public void registrarTarefas(String sessionId, List<AgentTask> tarefas) {
        if (sessionId == null || tarefas == null) return;
        taskLists.put(sessionId, new ArrayList<>(tarefas));
    }

    public AgentTask adicionarTarefa(String sessionId, String descricao) {
        List<AgentTask> tarefas = taskLists.computeIfAbsent(sessionId, k -> new ArrayList<>());
        int nextNum = tarefas.stream().mapToInt(AgentTask::getNumero).max().orElse(0) + 1;
        AgentTask newTask = new AgentTask(nextNum, descricao);
        tarefas.add(newTask);
        return newTask;
    }

    public AgentTask marcarTarefaConcluida(String sessionId, int numero) {
        List<AgentTask> tarefas = taskLists.get(sessionId);
        if (tarefas == null) return null;
        return tarefas.stream()
                .filter(t -> t.getNumero() == numero)
                .peek(t -> t.setConcluida(true))
                .findFirst()
                .orElse(null);
    }

    public AgentTask marcarTarefaConcluidaPorBusca(String sessionId, String busca) {
        List<AgentTask> tarefas = taskLists.get(sessionId);
        if (tarefas == null || busca == null) return null;
        String lowerBusca = busca.toLowerCase().trim();
        return tarefas.stream()
                .filter(t -> !t.isConcluida())
                .filter(t -> t.getDescricao().toLowerCase().contains(lowerBusca))
                .peek(t -> t.setConcluida(true))
                .findFirst()
                .orElse(null);
    }

    public List<AgentTask> getTarefas(String sessionId) {
        return taskLists.getOrDefault(sessionId, Collections.emptyList());
    }

    public void salvarConfianca(String sessionId, ConfidenceReport report) {
        confidenceReports.put(sessionId, report);
    }

    public void salvarCodigo(String sessionId, String codigo) {
        codeResults.put(sessionId, codigo);
    }

    public FullCycleResponse montarResposta(String sessionId) {
        AgentContext context = getContext(sessionId);
        String documentContent = context != null ? context.assembleDocument() : "";
        FullCycleRequest request = requests.get(sessionId);

        CodeGenerationResponse code = new CodeGenerationResponse();
        String codigoGerado = codeResults.get(sessionId);
        if (codigoGerado != null) code.setNotes(codigoGerado);

        FullCycleResponse response = new FullCycleResponse();
        response.setDocument(new DocumentResponse(documentContent, null));
        response.setCode(code);
        response.setConfidenceReport(confidenceReports.get(sessionId));
        response.setSpecOnly(request == null || request.isSpecOnly());
        response.setDocumentIterations(context != null ? context.getDocumentIteration() : 1);
        return response;
    }

    public void addLog(String sessionId, ProgressEvent event) {
        logHistory.computeIfAbsent(sessionId, k -> Collections.synchronizedList(new ArrayList<>())).add(event);
    }

    public List<ProgressEvent> getLogs(String sessionId) {
        return logHistory.getOrDefault(sessionId, Collections.emptyList());
    }

    public List<String> listActiveSessions() {
        return new ArrayList<>(contexts.keySet());
    }

    public void clear(String sessionId) {
        contexts.remove(sessionId);
        logHistory.remove(sessionId);
        taskLists.remove(sessionId);
        confidenceReports.remove(sessionId);
        codeResults.remove(sessionId);
        requests.remove(sessionId);
    }
}
