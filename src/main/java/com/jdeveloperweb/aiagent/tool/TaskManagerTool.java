package com.jdeveloperweb.aiagent.tool;

import com.jdeveloperweb.aiagent.dto.AgentTask;
import com.jdeveloperweb.aiagent.service.AgentIO;
import com.jdeveloperweb.aiagent.service.SessionStoreService;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TaskManagerTool {

    private final SessionStoreService sessionStore;
    private final AgentIO agentIO;

    public TaskManagerTool(SessionStoreService sessionStore, AgentIO agentIO) {
        this.sessionStore = sessionStore;
        this.agentIO = agentIO;
    }

    @Tool("Registra a lista de tarefas que serao executadas nesta sessao. " +
         "Chame ANTES de qualquer ferramenta de execucao. " +
         "Formato: uma tarefa por linha com numero no inicio (ex: '1. Escrever a secao a) CONTEXTO').")
    public String registrarTarefas(String sessionId, String tarefas) {
        Pattern p = Pattern.compile("^(\\d+)[.)\\s]+(.+)$");
        List<AgentTask> taskList = new ArrayList<>();
        for (String line : tarefas.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.isBlank()) continue;
            Matcher m = p.matcher(trimmed);
            if (m.find()) {
                int num = Integer.parseInt(m.group(1));
                String desc = m.group(2).trim();
                AgentTask task = new AgentTask(num, desc);
                
                // Inferencia basica de metadados
                if (desc.toLowerCase().contains("escrever a secao")) {
                    task.setTaskType("WRITE_SECTION");
                    task.setSectionId(extractSectionId(desc));
                } else if (desc.toLowerCase().contains("revisar")) {
                    task.setTaskType("REVIEW");
                } else if (desc.toLowerCase().contains("gerar codigo")) {
                    task.setTaskType("GENERATE_CODE");
                } else if (desc.toLowerCase().contains("confianca")) {
                    task.setTaskType("CONFIDENCE");
                }
                
                taskList.add(task);
            }
        }
        sessionStore.registrarTarefas(sessionId, taskList);
        agentIO.taskList(taskList);
        return "Tarefas registradas: " + taskList.size();
    }

    @Tool("Adiciona uma nova tarefa de OPERACAO DE NEGOCIO (b.N) a lista de execucao. " +
         "Use APENAS se descobrir um requisito funcional novo. " +
         "PROIBIDO usar para correcoes, revisoes ou reescritas.")
    public String adicionarTarefa(String sessionId, String descricao) {
        AgentTask newTask = sessionStore.adicionarTarefa(sessionId, descricao);
        
        // Inferencia basica
        if (descricao.toLowerCase().contains("escrever a secao")) {
            newTask.setTaskType("WRITE_SECTION");
            newTask.setSectionId(extractSectionId(descricao));
        }
        
        agentIO.step("Nova tarefa adicionada: " + descricao);
        return "Tarefa #" + newTask.getNumero() + " adicionada com sucesso.";
    }

    private String extractSectionId(String desc) {
        // Tenta extrair padroes como "a) CONTEXTO" ou "b.1) Salvar"
        Pattern p = Pattern.compile("([a-z]\\.[\\d.]+|[a-z]\\)\\s+[A-Z]+)");
        Matcher m = p.matcher(desc);
        if (m.find()) return m.group(1).trim();
        return desc;
    }

    public String marcarConcluida(String sessionId, int numeroDaTarefa) {
        AgentTask task = sessionStore.marcarTarefaConcluida(sessionId, numeroDaTarefa);
        if (task != null) agentIO.taskDone(task.getNumero(), task.getDescricao());
        return "Tarefa " + numeroDaTarefa + " concluida.";
    }
}
