package com.uniovi.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

@Service
public class LlmService {

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
    private static final String EMPATHY_API_URL = "https://empathyai.prod.empathy.co/v1/chat/completions";

    @Value("${GEMINI_API_KEY}")
    private String geminiApiKey;

    @Value("${EMPATHY_API_KEY}")
    private String empathyApiKey;

    private final RestTemplate restTemplate;

    public LlmService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getLLMResponse(String question, String model) {
        String requestJson;
        String url;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        switch (model.toLowerCase()) {
            case "gemini":
                url = GEMINI_API_URL + "?key=" + geminiApiKey;
                requestJson = String.format("{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}", question);
                break;
            case "empathy":
                url = EMPATHY_API_URL;
                headers.set("Authorization", "Bearer " + empathyApiKey);
                requestJson = String.format("{\"model\":\"qwen/Qwen2.5-Coder-7B-Instruct\",\"messages\":[{\"role\":\"system\",\"content\":\"You are a helpful assistant.\"},{\"role\":\"user\",\"content\":\"%s\"}]}", question);
                break;
            default:
                return "Model not supported.";
        }

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        return switch (model.toLowerCase()) {
            case "gemini" -> extractGeminiResponse(response.getBody());
            case "empathy" -> extractEmpathyResponse(response.getBody());
            default -> "Invalid model response.";
        };
    }

    private String extractGeminiResponse(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            return jsonNode.at("/candidates/0/content").asText();
        } catch (IOException e) {
            return "Error processing Gemini response.";
        }
    }

    private String extractEmpathyResponse(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            return jsonNode.at("/choices/0/message/content").asText();
        } catch (IOException e) {
            return "Error processing Empathy response.";
        }
    }
    // En LlmService
    public String getImageHint(String imageDescription, String question) {
        // Podríamos incluir más contexto o usar otro modelo si es necesario
        String fullQuestion = String.format("Aquí está una imagen con la descripción '%s'. %s", imageDescription, question);
        return getLLMResponse(fullQuestion, "gemini");  // O puedes usar otro modelo
    }

}
