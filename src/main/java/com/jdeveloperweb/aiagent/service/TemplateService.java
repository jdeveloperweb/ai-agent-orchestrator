package com.jdeveloperweb.aiagent.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class TemplateService {
    private final Map<String, String> templates = new HashMap<>();

    public TemplateService() {
        templates.put("full_cycle_plan", "Crie um plano estruturado com Contexto, Implementacao e Pontos de Funcao.");
    }

    public String getTemplate(String key) {
        return templates.getOrDefault(key, "Template nao encontrado para: " + key);
    }
}