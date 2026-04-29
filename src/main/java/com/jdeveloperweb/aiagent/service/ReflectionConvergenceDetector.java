package com.jdeveloperweb.aiagent.service;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Detecta quando o loop de reflexão não está convergindo — o revisor continua
 * apontando os mesmos problemas sem que o agente consiga resolvê-los.
 * Nesse caso, é mais útil parar e pedir intervenção humana do que continuar gastando tokens.
 */
@Component
public class ReflectionConvergenceDetector {

    private static final double STUCK_THRESHOLD = 0.72;
    private static final int MIN_WORD_LENGTH = 4;

    /**
     * Retorna true se os dois últimos feedbacks têm sobreposição alta de problemas,
     * indicando que o agente está preso e não vai melhorar sozinho.
     */
    public boolean isStuck(List<String> feedbackHistory) {
        if (feedbackHistory.size() < 2) return false;

        String last = feedbackHistory.get(feedbackHistory.size() - 1);
        String previous = feedbackHistory.get(feedbackHistory.size() - 2);

        Set<String> lastTokens = tokenize(last);
        Set<String> prevTokens = tokenize(previous);

        if (lastTokens.isEmpty() || prevTokens.isEmpty()) return false;

        long overlap = lastTokens.stream().filter(prevTokens::contains).count();
        double similarity = (double) overlap / Math.max(lastTokens.size(), prevTokens.size());

        return similarity >= STUCK_THRESHOLD;
    }

    /**
     * Extrai palavras-chave relevantes do feedback para comparação de similaridade.
     */
    public String buildStuckMessage(List<String> feedbackHistory) {
        if (feedbackHistory.isEmpty()) return "Problemas não identificados.";
        return feedbackHistory.get(feedbackHistory.size() - 1)
                .replaceFirst("^PRECISA_MELHORAR:\\s*", "")
                .trim();
    }

    private Set<String> tokenize(String text) {
        return Arrays.stream(text.toLowerCase().split("[\\s\\n\\d\\.\\-\\)\\(,:;]+"))
                .map(String::trim)
                .filter(w -> w.length() >= MIN_WORD_LENGTH)
                .collect(Collectors.toSet());
    }
}
