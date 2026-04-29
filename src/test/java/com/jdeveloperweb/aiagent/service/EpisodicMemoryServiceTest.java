package com.jdeveloperweb.aiagent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jdeveloperweb.aiagent.dto.SpecMemory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EpisodicMemoryServiceTest {

    private EpisodicMemoryService service;
    private ObjectMapper mapper;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        service = new EpisodicMemoryService(tempDir.toString(), mapper);
    }

    @Test
    @DisplayName("Should store and find similar memory entries")
    void shouldStoreAndFindSimilar() {
        service.store("Relatorio PDF", "Conteudo sobre geracao de relatorios em formato PDF usando Jasper.", "path1.md");
        service.store("Exportacao Excel", "Conteudo sobre exportacao de dados para planilhas Excel.", "path2.md");

        List<SpecMemory> results = service.findSimilar("Preciso gerar um PDF de relatorio", 1);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getObjective()).isEqualTo("Relatorio PDF");
    }

    @Test
    @DisplayName("Should return empty list when no match found")
    void shouldReturnEmptyWhenNoMatch() {
        service.store("Auth", "Login system", "path.md");
        List<SpecMemory> results = service.findSimilar("Something completely different", 1);
        assertThat(results).isEmpty();
    }
}
