package com.jdeveloperweb.aiagent.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
public class ConfluenceClient {

    private static final Logger log = LoggerFactory.getLogger(ConfluenceClient.class);
    private static final int MAX_CONTENT_CHARS = 12_000;

    private final String baseUrl;
    private final String authHeader;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public record ConfluencePage(String id, String title, String excerpt) {}

    public ConfluenceClient(
            @Value("${agent.confluence.url}") String baseUrl,
            @Value("${agent.confluence.email}") String email,
            @Value("${agent.confluence.api-token}") String token) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String creds = email + ":" + token;
        this.authHeader = "Basic " + Base64.getEncoder().encodeToString(creds.getBytes(StandardCharsets.UTF_8));

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(30_000);
        this.restTemplate = new RestTemplate(factory);
    }

    public List<ConfluencePage> searchPages(String term) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/rest/api/search")
                .queryParam("cql", "{cql}")
                .queryParam("limit", "8")
                .build(Map.of("cql", "text ~ \"" + term + "\" AND type = page"));

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    uri, HttpMethod.GET, new HttpEntity<>(buildHeaders()), String.class);
            return parseSearchResults(response.getBody());
        } catch (Exception e) {
            log.error("Erro ao buscar no Confluence (termo={}): {}", term, e.getMessage());
            return List.of();
        }
    }

    public String getPageContent(String pageId) {
        String url = baseUrl + "/rest/api/content/" + pageId.trim() + "?expand=body.storage";
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(buildHeaders()), String.class);
            return parsePageContent(response.getBody());
        } catch (Exception e) {
            log.error("Erro ao ler página Confluence (id={}): {}", pageId, e.getMessage());
            return "Erro ao ler página " + pageId + ": " + e.getMessage();
        }
    }

    private List<ConfluencePage> parseSearchResults(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        JsonNode results = root.path("results");
        List<ConfluencePage> pages = new ArrayList<>();
        for (JsonNode result : results) {
            // O ID real da página está em content.id, não no topo do objeto result
            String id = result.path("content").path("id").asText(null);
            String title = stripHtml(result.path("title").asText("")).trim();
            String excerpt = stripHtml(result.path("excerpt").asText(""));
            if (id != null && !id.isBlank()) {
                pages.add(new ConfluencePage(id, title, excerpt));
            }
        }
        return pages;
    }

    private String parsePageContent(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        String title = root.path("title").asText("Sem título");
        String storageHtml = root.path("body").path("storage").path("value").asText("");
        String plainText = stripHtml(storageHtml);
        String content = "# " + title + "\n\n" + plainText;
        if (content.length() > MAX_CONTENT_CHARS) {
            content = content.substring(0, MAX_CONTENT_CHARS) + "\n\n... [conteúdo truncado]";
        }
        return content;
    }

    private String stripHtml(String html) {
        if (html == null || html.isBlank()) return "";
        return html
                .replaceAll("@@@hl@@@", "")
                .replaceAll("@@@endhl@@@", "")
                .replaceAll("<[^>]+>", " ")
                .replaceAll("&nbsp;", " ")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&amp;", "&")
                .replaceAll("&quot;", "\"")
                .replaceAll("\\s{2,}", " ")
                .trim();
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authHeader);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}
