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
import java.util.List;
import java.util.Map;

@Service
public class LlmService {

    @Value("${API_KEY}")
    private String apiKey;

    private HttpHeaders headers;

    private final RestTemplate restTemplate = new RestTemplate();

    public String sendQuestionToLLM(String question, String imageUrl) {
        String url = "https://empathyai.prod.empathy.co/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + apiKey);

        // Si se pasa una URL de imagen, incluimos esta en el mensaje
        String imageMessage = imageUrl != null && !imageUrl.isEmpty()
                ? "¿Puedes decirme que se puede ver en la imagen siguiente?: " + imageUrl
                : "";

        // Construcción del cuerpo de la solicitud
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(Map.of(
                "model", "mistralai/Mistral-7B-Instruct-v0.3",
                "messages", new Object[]{
                        Map.of("role", "system", "content", "You are a helpful assistant."),
                        Map.of("role", "user", "content", question),
                        // Si se pasa una URL de imagen, lo agregamos como parte de la respuesta del asistente
                        imageMessage.isEmpty() ? null : Map.of("role", "assistant", "content", imageMessage)
                }
        ), headers);

        try {
            // Enviar la solicitud POST a la API
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            // Extraer y procesar la respuesta
            return extractResponse(response.getBody());
        } catch (Exception e) {
            // Manejo de errores
            System.err.println("Error en la consulta: " + e.getMessage());
            return "Error al comunicarse con la API";
        }
    }

    private String extractResponse(Map<?, ?> response) {
        try {
            return ((Map<String, String>) ((Map<String, Object>) ((List<Object>) response.get("choices")).get(0)).get("message")).get("content").toString();
        } catch (Exception e) {
            return "Error processing response";
        }
    }
}
