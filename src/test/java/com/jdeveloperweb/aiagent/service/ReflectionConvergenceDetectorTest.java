package com.jdeveloperweb.aiagent.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class ReflectionConvergenceDetectorTest {

    private final ReflectionConvergenceDetector detector = new ReflectionConvergenceDetector();

    @Test
    @DisplayName("Should detect high similarity between feedbacks")
    void shouldDetectStuck() {
        String feedback1 = "PRECISA_MELHORAR: Falta o nome da tabela TB_CLIENTES e o status HTTP 404.";
        String feedback2 = "PRECISA_MELHORAR: Voce esqueceu de colocar a tabela TB_CLIENTES e o retorno HTTP 404.";

        boolean stuck = detector.isStuck(List.of(feedback1, feedback2));

        assertThat(stuck).isTrue();
    }

    @Test
    @DisplayName("Should not detect high similarity when feedback changes significantly")
    void shouldNotBeStuck() {
        String feedback1 = "PRECISA_MELHORAR: Adicione a tabela TB_CLIENTES.";
        String feedback2 = "PRECISA_MELHORAR: Agora falta validar o campo email no contrato.";

        boolean stuck = detector.isStuck(List.of(feedback1, feedback2));

        assertThat(stuck).isFalse();
    }

    @Test
    @DisplayName("Should handle empty or single feedback")
    void shouldHandleEdgeCases() {
        assertThat(detector.isStuck(List.of())).isFalse();
        assertThat(detector.isStuck(List.of("Feedback 1"))).isFalse();
    }
}
