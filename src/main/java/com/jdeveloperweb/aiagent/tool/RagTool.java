package com.jdeveloperweb.aiagent.tool;

import com.jdeveloperweb.aiagent.client.RagClient;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RagTool {

    private static final Logger log = LoggerFactory.getLogger(RagTool.class);
    private final RagClient ragClient;
    private final com.jdeveloperweb.aiagent.service.AgentIO agentIO;

    public RagTool(RagClient ragClient, com.jdeveloperweb.aiagent.service.AgentIO agentIO) {
        this.ragClient = ragClient;
        this.agentIO = agentIO;
    }

    @Tool("""
        Consulta a base de conhecimento TECNICO do projeto. Use EXCLUSIVAMENTE para encontrar:
        nomes reais de tabelas e colunas do BANCO DE DADOS, endpoints técnicos e contratos de APIs.
        NAO use para regras de negocio ou entendimento funcional; para isso, use as ferramentas do CONFLUENCE.
        """)
    public String consultarRag(String pergunta) {
        agentIO.toolStart("🔍 Pesquisando no RAG: " + pergunta);
        log.info("Consultando RAG com a pergunta: {}", pergunta);
        String result = ragClient.askRag(pergunta);
        agentIO.toolEnd("RAG retornou dados técnicos.", result);
        agentIO.persistRagInsight("RAG", pergunta, result);
        return result;
    }
}
