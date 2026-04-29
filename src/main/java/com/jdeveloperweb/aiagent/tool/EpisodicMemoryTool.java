package com.jdeveloperweb.aiagent.tool;

import com.jdeveloperweb.aiagent.dto.SpecMemory;
import com.jdeveloperweb.aiagent.service.EpisodicMemoryService;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EpisodicMemoryTool {

    private final EpisodicMemoryService memoryService;

    public EpisodicMemoryTool(EpisodicMemoryService memoryService) {
        this.memoryService = memoryService;
    }

    @Tool("Busca especificações técnicas similares já aprovadas no histórico do sistema, para usar como referência de padrão e qualidade.")
    public String buscarEspecificacoesSimilares(String descricao) {
        List<SpecMemory> similar = memoryService.findSimilar(descricao, 3);

        if (similar.isEmpty()) {
            return "Nenhuma especificação similar encontrada no histórico.";
        }

        return similar.stream()
                .map(m -> String.format(
                        "--- Especificação anterior ---\nObjetivo: %s\nData: %s\nResumo:\n%s",
                        m.getObjective(),
                        m.getCreatedAt() != null ? m.getCreatedAt().toString() : "desconhecida",
                        m.getContentPreview()
                ))
                .collect(Collectors.joining("\n\n"));
    }
}
