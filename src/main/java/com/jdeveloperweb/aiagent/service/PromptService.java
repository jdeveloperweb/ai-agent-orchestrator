package com.jdeveloperweb.aiagent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PromptService {

    private static final Logger log = LoggerFactory.getLogger(PromptService.class);
    private final ResourceLoader resourceLoader;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public PromptService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public String load(String promptName) {
        return cache.computeIfAbsent(promptName, name -> {
            try {
                Resource resource = resourceLoader.getResource("classpath:prompts/" + name + ".txt");
                return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error("Erro ao carregar prompt {}: {}", name, e.getMessage());
                return "";
            }
        });
    }

    public String loadAndReplace(String promptName, Map<String, Object> variables) {
        String template = load(promptName);
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            template = template.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
        }
        return template;
    }

    public String buildSectionPrompt(AgentContext context, String sectionKey, String sectionTemplate) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("objective", context.getObjective());
        
        String analysisPlan = context.getAnalysisPlan();
        vars.put("analysisPlan", (analysisPlan != null && !analysisPlan.isBlank()) 
            ? "=== PLANO DE ANALISE ===\n" + analysisPlan + "\n" : "");

        List<String> ragInsights = context.getRagInsights();
        if (!ragInsights.isEmpty()) {
            StringBuilder sb = new StringBuilder("=== PESQUISAS JA REALIZADAS (use como contexto base) ===\n");
            ragInsights.stream().limit(12).forEach(i -> sb.append("- ").append(i).append("\n"));
            vars.put("ragInsights", sb.toString());
        } else {
            vars.put("ragInsights", "");
        }

        Map<String, String> sectionResults = context.getSectionResults();
        if (!sectionResults.isEmpty()) {
            StringBuilder sb = new StringBuilder("=== CONTEXTO DAS SECOES ANTERIORES ===\n");
            List<String> keys = new ArrayList<>(sectionResults.keySet());
            for (int i = 0; i < keys.size(); i++) {
                String k = keys.get(i);
                String v = sectionResults.get(k);
                if (i == keys.size() - 1) {
                    sb.append("-- ").append(k).append(" (Integra) --\n").append(v).append("\n\n");
                } else {
                    sb.append("-- ").append(k).append(" (Resumo) --\n").append(summarize(v)).append("\n\n");
                }
            }
            vars.put("previousSections", sb.toString());
        } else {
            vars.put("previousSections", "");
        }

        List<String> reflectionHistory = context.getReflectionHistory();
        if (!reflectionHistory.isEmpty()) {
            StringBuilder sb = new StringBuilder("=== CORRECOES OBRIGATORIAS DAS REVISOES ANTERIORES ===\n");
            reflectionHistory.forEach(f -> sb.append("- ").append(f).append("\n"));
            vars.put("reflectionHistory", sb.toString());
        } else {
            vars.put("reflectionHistory", "");
        }

        vars.put("sectionKey", sectionKey);
        vars.put("sectionTemplate", sectionTemplate);

        return loadAndReplace("section-writer", vars);
    }

    public String summarize(String content) {
        if (content == null || content.length() < 500) return content;

        StringBuilder summary = new StringBuilder(content.substring(0, Math.min(content.length(), 1000)));
        summary.append("\n... [resumo automatico] ...\n");

        Set<String> extracted = new LinkedHashSet<>();

        // Tabelas
        java.util.regex.Matcher mt = java.util.regex.Pattern.compile(
            "(?:Tabela:|tabela\\s+`?)([A-Za-z_][A-Za-z0-9_]{2,})|\\b(TB_[A-Z0-9_]+)\\b",
            java.util.regex.Pattern.CASE_INSENSITIVE
        ).matcher(content);
        while (mt.find()) {
            String name = mt.group(1) != null ? mt.group(1) : mt.group(2);
            extracted.add("Tabela: " + name);
        }

        // Status HTTP
        java.util.regex.Matcher mh = java.util.regex.Pattern.compile(
            "(?:HTTP|status|retornar)\\s+(\\d{3})[^.\\n]{0,80}",
            java.util.regex.Pattern.CASE_INSENSITIVE
        ).matcher(content);
        while (mh.find()) {
            extracted.add("HTTP " + mh.group(1) + ": " + mh.group(0).trim());
        }

        // Sistemas
        java.util.regex.Matcher ms = java.util.regex.Pattern.compile(
            "(?:Sistema|Servico|Service|API|modulo)\\s+([A-Z][A-Za-z0-9_-]{2,})",
            java.util.regex.Pattern.CASE_INSENSITIVE
        ).matcher(content);
        while (ms.find()) {
            extracted.add("Sistema: " + ms.group(1));
        }

        if (!extracted.isEmpty()) {
            summary.append("\n=== REFERENCIAS-CHAVE DA SECAO ===\n");
            extracted.stream().limit(20).forEach(e -> summary.append("- ").append(e).append("\n"));
        }

        return summary.toString();
    }
}
