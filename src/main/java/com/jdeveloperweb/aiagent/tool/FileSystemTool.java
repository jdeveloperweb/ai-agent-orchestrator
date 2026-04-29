package com.jdeveloperweb.aiagent.tool;

import com.jdeveloperweb.aiagent.service.AgentIO;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class FileSystemTool {

    private static final Logger log = LoggerFactory.getLogger(FileSystemTool.class);
    private final Path rootDir;
    private final AgentIO agentIO;

    public FileSystemTool(@Value("${agent.workspace.root-dir}") String rootDirPath, AgentIO agentIO) throws IOException {
        this.rootDir = Paths.get(rootDirPath).toAbsolutePath().normalize();
        this.agentIO = agentIO;
        if (!Files.exists(this.rootDir)) {
            Files.createDirectories(this.rootDir);
        }
    }

    @Tool("Salva um conteúdo em um arquivo local dentro do workspace.")
    public String salvarArquivo(String caminhoRelativo, String conteudo) {
        try {
            Path targetPath = resolvePath(caminhoRelativo);
            
            if (!Files.exists(targetPath.getParent())) {
                agentIO.toolStart("📁 Criando diretório: " + rootDir.relativize(targetPath.getParent()));
                Files.createDirectories(targetPath.getParent());
            }

            agentIO.toolStart("✍️ Gravando arquivo: " + caminhoRelativo + " (" + conteudo.length() + " bytes)");
            Files.writeString(targetPath, conteudo, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            
            agentIO.fileCreated(caminhoRelativo); // Garantindo a notificação
            log.info("Evento FILE_CREATED enviado para: {}", caminhoRelativo);
            
            return "Arquivo salvo com sucesso em: " + caminhoRelativo;
        } catch (Exception e) {
            log.error("Erro ao salvar arquivo: {}", caminhoRelativo, e);
            agentIO.info("ERRO ao salvar arquivo: " + e.getMessage());
            return "Erro ao salvar arquivo: " + e.getMessage();
        }
    }

    @Tool("Lê o conteúdo de um arquivo local dentro do workspace.")
    public String lerArquivo(String caminhoRelativo) {
        try {
            Path targetPath = resolvePath(caminhoRelativo);
            if (!Files.exists(targetPath)) {
                return "Arquivo não encontrado: " + caminhoRelativo;
            }
            agentIO.toolStart("Lendo arquivo: " + caminhoRelativo);
            return Files.readString(targetPath);
        } catch (Exception e) {
            log.error("Erro ao ler arquivo: {}", caminhoRelativo, e);
            return "Erro ao ler arquivo: " + e.getMessage();
        }
    }

    @Tool("Lista os arquivos e diretórios em uma pasta dentro do workspace.")
    public List<String> listarArquivos(String pastaRelativa) {
        try {
            Path targetPath = resolvePath(pastaRelativa);
            if (!Files.exists(targetPath) || !Files.isDirectory(targetPath)) {
                return List.of("Diretório não encontrado: " + pastaRelativa);
            }
            agentIO.toolStart("Listando diretório: " + pastaRelativa);
            try (Stream<Path> stream = Files.list(targetPath)) {
                return stream.map(p -> rootDir.relativize(p).toString())
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("Erro ao listar arquivos em: {}", pastaRelativa, e);
            return List.of("Erro ao listar arquivos: " + e.getMessage());
        }
    }

    private Path resolvePath(String relativePath) {
        Path path = rootDir.resolve(relativePath).normalize();
        if (!path.startsWith(rootDir)) {
            throw new SecurityException("Path traversal attempt detected: " + relativePath);
        }
        return path;
    }
}
