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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

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

    /**
     * Envía una pregunta a Gemini o Empathy y devuelve la respuesta generada.
     */
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

    /**
     * Envía una imagen en Base64 junto con una pregunta a Gemini o Empathy y obtiene una respuesta.
     */
    public String getLLMResponseWithImage(String imageUrl, String question, String model) throws IOException {
        byte[] imageBytes = downloadImage(imageUrl);
        if (imageBytes == null) {
            return "Error downloading image.";
        }
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        String requestJson;
        String url;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        switch (model.toLowerCase()) {
            case "gemini":
                url = "https://api.gemini.com/v1/generate?key=" + geminiApiKey;
                requestJson = String.format(
                        "{ \"contents\": [ { \"parts\": [ " +
                                "{ \"inline_data\": { \"mime_type\": \"image/jpeg\", \"data\": \"%s\" } }, " +
                                "{ \"text\": \"%s\" } ] } ] }",
                        base64Image, question
                );
                break;
            case "empathy":
                url = "https://api.empathy.com/v1/generate";
                headers.set("Authorization", "Bearer " + empathyApiKey);
                requestJson = String.format(
                        "{ \"model\": \"qwen/Qwen2.5-Coder-7B-Instruct\", \"messages\": [ " +
                                "{ \"role\": \"system\", \"content\": \"You are a helpful assistant.\" }, " +
                                "{ \"role\": \"user\", \"content\": \"%s\" }, " +
                                "{ \"role\": \"user\", \"content\": \"data:image/jpeg;base64,%s\" } ] }",
                        question, base64Image
                );
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

    private byte[] downloadImage(String imageUrl) {
        try (InputStream in = new URL(imageUrl).openStream();
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] temp = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(temp)) != -1) {
                buffer.write(temp, 0, bytesRead);
            }
            return buffer.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extrae la respuesta de Gemini desde el JSON de la API.
     */
    private String extractGeminiResponse(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            return jsonNode.at("/candidates/0/content").asText();
        } catch (IOException e) {
            return "Error processing Gemini response.";
        }
    }

    /**
     * Extrae la respuesta de Empathy desde el JSON de la API.
     */
    private String extractEmpathyResponse(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            return jsonNode.at("/choices/0/message/content").asText();
        } catch (IOException e) {
            return "Error processing Empathy response.";
        }
    }

    /**
     * Usa una imagen y una pregunta para obtener una respuesta del modelo.
     */
    public String getImageHint(String imageUrl, String question, String model) {
        try {
            return getLLMResponseWithImage(imageUrl, question, model);
        } catch (IOException e) {
            return "Error reading image file.";
        }
    }
}
