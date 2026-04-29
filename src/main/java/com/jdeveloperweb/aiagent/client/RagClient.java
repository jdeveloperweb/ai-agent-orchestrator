package com.jdeveloperweb.aiagent.client;

import com.jdeveloperweb.aiagent.dto.RagChatRequest;
import com.jdeveloperweb.aiagent.dto.RagChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RagClient {

    private final RestClient restClient;
    private final String chatPath;
    private final java.util.Map<String, String> cache = new java.util.concurrent.ConcurrentHashMap<>();

    public RagClient(RestClient.Builder builder, 
                     @Value("${agent.rag.base-url}") String baseUrl,
                     @Value("${agent.rag.chat-path}") String chatPath) {
        
        // Aumentando o timeout para 60 segundos (RAG pode ser lento)
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(60000);

        this.restClient = builder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
        this.chatPath = chatPath;
    }

    public String askRag(String question) {
        if (cache.containsKey(question)) {
            return cache.get(question);
        }
        try {
            RagChatRequest request = new RagChatRequest(question);
            request.setProvider("OPENAI");
            request.setUseSpringAi(true);

            RagChatResponse response = restClient.post()
                    .uri(chatPath)
                    .body(request)
                    .retrieve()
                    .body(RagChatResponse.class);
            
            String answer = response != null ? response.getAnswer() : "O RAG retornou uma resposta vazia.";
            cache.put(question, answer);
            return answer;
        } catch (Exception e) {
            return "Erro ao consultar RAG (Timeout ou Conexão): " + e.getMessage();
        }
    }
}
