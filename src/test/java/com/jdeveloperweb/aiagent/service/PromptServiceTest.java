package com.jdeveloperweb.aiagent.service;

import com.jdeveloperweb.aiagent.dto.AgentContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PromptServiceTest {

    private PromptService promptService;
    private ResourceLoader resourceLoader = new DefaultResourceLoader();

    @BeforeEach
    void setUp() {
        promptService = new PromptService(resourceLoader);
    }

    @Test
    @DisplayName("Should summarize long content correctly")
    void shouldSummarizeLongContent() {
        String longContent = "Tabela: TB_USUARIOS. Este e um conteudo muito longo que deve ser resumido pelo algoritmo de regex. " +
                "O sistema Service_Auth deve ser validado. Retornar HTTP 200 OK.";
        // Create content > 500 chars to trigger summary
        StringBuilder sb = new StringBuilder(longContent);
        while (sb.length() < 600) {
            sb.append(" Mais texto para encher linguiça e passar de 500 caracteres.");
        }

        String summary = promptService.summarize(sb.toString());
        
        assertThat(summary).contains("... [resumo automatico] ...");
        assertThat(summary).contains("Tabela: TB_USUARIOS");
        assertThat(summary).contains("Sistema: Service_Auth");
    }

    @Test
    @DisplayName("Should build section prompt correctly")
    void shouldBuildSectionPrompt() {
        AgentContext context = new AgentContext("Test Objective");
        context.setAnalysisPlan("Do step 1.");
        
        String prompt = promptService.buildSectionPrompt(context, "a) CONTEXTO", "Format: markdown");
        
        assertThat(prompt).contains("=== SOLICITACAO GERAL ===");
        assertThat(prompt).contains("Test Objective");
        assertThat(prompt).contains("=== PLANO DE ANALISE ===");
        assertThat(prompt).contains("Do step 1.");
        assertThat(prompt).contains("Gere SOMENTE a secao: a) CONTEXTO");
    }
}
