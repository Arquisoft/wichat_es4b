package com.uniovi.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class LlmService {

    private static final String EMPATHY_API_URL = "https://empathyai.prod.empathy.co/v1/chat/completions";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=";
    private static final String EMPATHY = "Empathy";
    private static final String PARTS = "parts";


    @Value("${llm.api.key}")
    private String llmApiKey;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String sendQuestionToLLM(String question, String ai,List<String> llmAnswers) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        if(ai.equals(EMPATHY)){
            headers.add("Authorization", "Bearer " + llmApiKey);

            // Construcción del cuerpo de la solicitud
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(Map.of(
                    "model", "mistralai/Mistral-7B-Instruct-v0.3",
                    "max_tokens", 1000,
                    "messages", List.of(
                            Map.of("role", "system", "content",
                                    "You are a quiz game assistant that helps players on thinking what cities and locations are shown in the images. "
                                            + "Provide soft clues as hints to guide the user without telling" +
                                            " him the correct answer. Always answer in the language specified by the user."),
                            Map.of("role", "user", "content", question)
                    )


            ), headers);

            try {
                // Enviar la solicitud POST a la API
                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(EMPATHY_API_URL, HttpMethod.POST, request,
                        (Class<Map<String, Object>>) (Class<?>) Map.class);

                // Extraer y procesar la respuesta
                return extractEmpathyResponse(response.getBody());
            } catch (Exception e) {
                // Manejo de errores
                System.err.println("Error en la consulta: " + e.getMessage());
                return "Error al comunicarse con la API";
            }
        }else{
            List<Map<String, Object>> contents = new ArrayList<>();

// Primero agregamos todos los mensajes pasados del modelo
            if (llmAnswers != null && !llmAnswers.isEmpty()) {
                for (String answer : llmAnswers) {
                    contents.add(Map.of(
                            "role", "model",
                            PARTS, List.of(
                                    Map.of("text", answer)
                            )
                    ));
                }
            }

            contents.add(Map.of(
                    "role", "user",
                    PARTS, List.of(
                            Map.of("text", "You are a quiz game assistant that helps players on thinking what cities and locations are shown in the images. "
                                    + "Provide soft clues as hints to guide the user without telling"
                                    + " him the correct answer. Always answer in the language specified by the user. "
                                    + "Hints must not be too helpful and only one and different from your last answer if you have answered me before."
                                    + "Do not overextend in your answers, just tell me the hint directly without any conversational start"),
                            Map.of("text", question)
                    )
            ));


            HttpEntity<Map<String, Object>> request = new HttpEntity<>(Map.of(
                    "contents", contents
            ), headers);

            try {
                // Enviar la solicitud POST a la API
                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                        GEMINI_API_URL + geminiApiKey,
                        HttpMethod.POST,
                        request,
                        (Class<Map<String, Object>>) (Class<?>) Map.class
                );

                // Extraer y procesar la respuesta
                return extractGeminiResponse(response.getBody());
            } catch (Exception e) {
                System.err.println("Error en la consulta a Gemini: " + e.getMessage());
                return "Error al comunicarse con Gemini API";
            }
        }

    }

    private String extractEmpathyResponse(Map<String, Object> response) {
            try {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, String> message = (Map<String, String>) firstChoice.get("message");
                    return message.getOrDefault("content", "Respuesta vacía");
                }
                return "Sin respuestas válidas en la API";
            } catch (Exception e) {
                return "Error procesando la respuesta";
            }
    }

    private String extractGeminiResponse(Map<String, Object> response) {

            try {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> firstCandidate = candidates.get(0);
                    Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        Map<String, Object> firstPart = parts.get(0);
                        return (String) firstPart.getOrDefault("text", "Respuesta vacía");
                    }
                }
                return "Sin respuestas válidas en la API de Gemini";
            } catch (Exception e) {
                return "Error procesando la respuesta de Gemini";
            }
        }

}
