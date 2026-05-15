package com.observai.alert.client;

import com.observai.alert.config.AliyunAiProperties;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class AliyunAiClient {
    private final RestClient restClient;
    private final AliyunAiProperties properties;

    public AliyunAiClient(RestClient aliyunAiRestClient, AliyunAiProperties properties) {
        this.restClient = aliyunAiRestClient;
        this.properties = properties;
    }

    public String chat(String systemPrompt, String userPrompt) {
        if (!properties.hasApiKey()) {
            throw new IllegalStateException("ALIYUN_AI_API_KEY is not configured");
        }

        ChatRequest request = new ChatRequest(
                properties.getModel(),
                List.of(
                        new ChatMessage("system", systemPrompt),
                        new ChatMessage("user", userPrompt)
                ),
                0.2
        );

        try {
            ChatResponse response = restClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(ChatResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new IllegalStateException("Aliyun AI returned empty response");
            }

            String content = response.choices().get(0).message().content();
            if (content == null || content.isBlank()) {
                throw new IllegalStateException("Aliyun AI returned empty content");
            }
            return content.trim();
        } catch (RestClientResponseException ex) {
            throw new IllegalStateException(
                    "Aliyun AI request failed: HTTP " + ex.getStatusCode().value() + " " + ex.getResponseBodyAsString(),
                    ex
            );
        }
    }

    private record ChatRequest(String model, List<ChatMessage> messages, double temperature) {
    }

    private record ChatMessage(String role, String content) {
    }

    private record ChatResponse(List<Choice> choices) {
    }

    private record Choice(ChatMessage message) {
    }
}
