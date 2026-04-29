package com.jdeveloperweb.aiagent.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AgentContextTest {

    @Test
    @DisplayName("Should correctly assemble document from sections")
    void shouldAssembleDocument() {
        AgentContext context = new AgentContext("Test Objective");
        context.putSection("a) CONTEXTO", "Initial context");
        context.putSection("b.1) API", "API implementation");
        context.putSection("c) PONTOS", "10 PF");

        String doc = context.assembleDocument();

        assertThat(doc).contains("Initial context");
        assertThat(doc).contains("## b) IMPLEMENTAÇÕES:");
        assertThat(doc).contains("API implementation");
        assertThat(doc).contains("10 PF");
    }

    @Test
    @DisplayName("Should summarize long content correctly")
    void shouldSummarizeLongContent() {
        AgentContext context = new AgentContext("Test");
        String longContent = "Tabela: TB_USUARIOS. Este e um conteudo muito longo que deve ser resumido pelo algoritmo de regex. " +
                "O sistema Service_Auth deve ser validado. Retornar HTTP 200 OK.";
        // Create content > 500 chars to trigger summary
        StringBuilder sb = new StringBuilder(longContent);
        while (sb.length() < 600) {
            sb.append(" Mais texto para encher linguiça e passar de 500 caracteres.");
        }

        // Accessing private summarize via public method if possible, or testing buildSectionPrompt
        String prompt = context.buildSectionPrompt("test", "template");
        
        // We can't easily test private summarize directly without reflection or making it protected,
        // but we can check if the assembled prompt contains parts of the summary logic.
        // Let's test buildSectionPrompt which uses summarize.
        
        context.putSection("prev", sb.toString());
        String sectionPrompt = context.buildSectionPrompt("next", "tpl");
        
        assertThat(sectionPrompt).contains("=== CONTEXTO DAS SECOES ANTERIORES ===");
        assertThat(sectionPrompt).contains("TB_USUARIOS");
        assertThat(sectionPrompt).contains("Service: Service_Auth");
    }
}
