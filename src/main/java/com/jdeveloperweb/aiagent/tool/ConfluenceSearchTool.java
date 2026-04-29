package com.jdeveloperweb.aiagent.tool;

import com.jdeveloperweb.aiagent.client.ConfluenceClient;
import com.jdeveloperweb.aiagent.service.AgentIO;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ConfluenceSearchTool {

    private final ConfluenceClient confluenceClient;
    private final AgentIO agentIO;

    public ConfluenceSearchTool(ConfluenceClient confluenceClient, AgentIO agentIO) {
        this.confluenceClient = confluenceClient;
        this.agentIO = agentIO;
    }

    @Tool("Busca REGRAS DE NEGOCIO, fluxos funcionais e documentos de entendimento no CONFLUENCE. " +
          "Retorna uma lista de paginas encontradas com seus IDs e resumos.")
    public String pesquisarNegocio(String termo) {
        agentIO.toolStart("🔍 Pesquisando Negócio no Confluence: " + termo);
        List<ConfluenceClient.ConfluencePage> pages = confluenceClient.searchPages(termo);

        if (pages.isEmpty()) {
            String msg = "Nenhuma página encontrada no Confluence para: " + termo;
            agentIO.toolEnd("Confluence retornou vazio.", msg);
            return msg;
        }

        String result = pages.stream()
                .map(p -> String.format("- Titulo: %s\n  ID: %s\n  Resumo: %s", p.title(), p.id(), p.excerpt()))
                .collect(Collectors.joining("\n\n"));

        agentIO.toolEnd("Confluence retornou " + pages.size() + " páginas.", result);
        agentIO.persistRagInsight("CONFLUENCE-BUSCA", termo, result);
        return result;
    }

    @Tool("Le o CONTEUDO COMPLETO de uma pagina do CONFLUENCE usando o ID da pagina. " +
          "Use esta ferramenta apos 'pesquisarNegocio' para aprofundar o entendimento em uma pagina específica.")
    public String lerDocumentacaoNegocio(String idPagina) {
        agentIO.toolStart("📖 Lendo documentação detalhada (ID: " + idPagina + ")");
        String result = confluenceClient.getPageContent(idPagina);
        agentIO.toolEnd("Página Confluence lida.", "Conteúdo de " + idPagina + " carregado.");
        agentIO.persistRagInsight("CONFLUENCE-PAGINA", idPagina, result);
        return result;
    }
}
