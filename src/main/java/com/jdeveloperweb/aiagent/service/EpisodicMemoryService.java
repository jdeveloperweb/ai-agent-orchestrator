package com.jdeveloperweb.aiagent.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jdeveloperweb.aiagent.dto.SpecMemory;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Memória episódica: armazena especificações aprovadas e permite busca por similaridade.
 * O agente usa isso para aprender com trabalhos anteriores — como um analista sênior
 * que lembra de casos similares já resolvidos.
 */
@Service
public class EpisodicMemoryService {

    private static final Logger log = LoggerFactory.getLogger(EpisodicMemoryService.class);
    private static final int PREVIEW_LENGTH = 600;
    private static final int MIN_KEYWORD_LENGTH = 4;
    private static final int MAX_RESULTS = 3;

    private final Path memoryDir;
    private final ObjectMapper mapper;
    private final List<SpecMemory> index = new ArrayList<>();

    public EpisodicMemoryService(
            @Value("${agent.memory.dir:./agent-workspace/memory}") String memoryDirPath,
            ObjectMapper mapper) throws IOException {
        this.memoryDir = Paths.get(memoryDirPath).toAbsolutePath().normalize();
        this.mapper = mapper;
        Files.createDirectories(memoryDir);
    }

    @PostConstruct
    public void loadIndex() {
        Path indexFile = memoryDir.resolve("index.json");
        if (!Files.exists(indexFile)) return;
        try {
            List<SpecMemory> loaded = mapper.readValue(indexFile.toFile(), new TypeReference<>() {});
            index.addAll(loaded);
            log.info("Memória episódica carregada: {} especificações.", index.size());
        } catch (IOException e) {
            log.warn("Não foi possível carregar o índice de memória episódica: {}", e.getMessage());
        }
    }

    public void store(String objective, String content, String filePath) {
        try {
            String id = UUID.randomUUID().toString();
            String preview = content.length() > PREVIEW_LENGTH ? content.substring(0, PREVIEW_LENGTH) + "..." : content;

            SpecMemory memory = new SpecMemory(id, objective, filePath, preview,
                    LocalDateTime.now(), extractKeywords(objective + " " + content));

            // Save full content
            Files.writeString(memoryDir.resolve(id + ".md"), content, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // Update index
            index.add(memory);
            persistIndex();

            log.info("Especificação armazenada na memória episódica: {}", id);
        } catch (IOException e) {
            log.warn("Falha ao armazenar na memória episódica: {}", e.getMessage());
        }
    }

    public List<SpecMemory> findSimilar(String query, int maxResults) {
        Set<String> queryTokens = new HashSet<>(extractKeywords(query));
        if (queryTokens.isEmpty()) return Collections.emptyList();

        return index.stream()
                .map(entry -> {
                    long overlap = entry.getKeywords().stream()
                            .filter(queryTokens::contains)
                            .count();
                    double score = (double) overlap / Math.max(queryTokens.size(), entry.getKeywords().size());
                    return Map.entry(entry, score);
                })
                .filter(e -> e.getValue() > 0.1)
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(maxResults > 0 ? maxResults : MAX_RESULTS)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public String getFullContent(String id) {
        try {
            Path file = memoryDir.resolve(id + ".md");
            if (Files.exists(file)) return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("Falha ao ler conteúdo da memória {}: {}", id, e.getMessage());
        }
        return null;
    }

    private List<String> extractKeywords(String text) {
        return Arrays.stream(text.toLowerCase().split("[\\s\\n\\W]+"))
                .filter(w -> w.length() >= MIN_KEYWORD_LENGTH)
                .distinct()
                .collect(Collectors.toList());
    }

    private void persistIndex() throws IOException {
        mapper.writeValue(memoryDir.resolve("index.json").toFile(), index);
    }
}
