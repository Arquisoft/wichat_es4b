package com.uniovi.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

@Service
public class LlmService {

    private static final String API_URL = "https://empathyai.prod.empathy.co/v1/chat/completions";

    @Value("${llm.api.key}")
    private String llmApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String sendQuestionToLLM(String question) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + llmApiKey);

        // Construcción del cuerpo de la solicitud
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(Map.of(
                "model", "mistralai/Mistral-7B-Instruct-v0.3",
                "messages", List.of(
                        Map.of("role", "system", "content", "" +
                                "You are a quiz game assistant that helps players on thinking what cities and locations are shown in the images. "
                                + "Provide soft clues as hints to guide the user without telling" +
                                " him the correct answer. Always respond in the language specified by the user."),
                        Map.of("role", "user", "content", question)
                )
        ), headers);

        try {
            // Enviar la solicitud POST a la API
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(API_URL, HttpMethod.POST, request,
                    (Class<Map<String, Object>>) (Class<?>) Map.class);

            // Extraer y procesar la respuesta
            return extractResponse(response.getBody());
        } catch (Exception e) {
            // Manejo de errores
            System.err.println("Error en la consulta: " + e.getMessage());
            e.printStackTrace();
            return "Error al comunicarse con la API";
        }
    }

    private String extractResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, String> message = (Map<String, String>) firstChoice.get("message");
                return message.getOrDefault("content", "Respuesta vacía");
            }
            return "Sin respuestas válidas en la API";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error procesando la respuesta";
        }
    }
}
