package com.jdeveloperweb.aiagent.dto;

public class AgentTask {

    private int numero;
    private String descricao;
    private String sectionId; // ex: "a) CONTEXTO", "b.1"
    private String taskType;  // ex: "WRITE_SECTION", "REVIEW", "GENERATE_CODE"
    private boolean concluida;

    public AgentTask(int numero, String descricao) {
        this.numero = numero;
        this.descricao = descricao;
        this.concluida = false;
    }

    public AgentTask(int numero, String descricao, String sectionId, String taskType) {
        this.numero = numero;
        this.descricao = descricao;
        this.sectionId = sectionId;
        this.taskType = taskType;
        this.concluida = false;
    }

    public int getNumero() { return numero; }
    public String getDescricao() { return descricao; }
    public String getSectionId() { return sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public boolean isConcluida() { return concluida; }
    public void setConcluida(boolean concluida) { this.concluida = concluida; }
}
