package com.uniovi.services;

import jakarta.servlet.http.HttpSession;
import org.hibernate.Session;
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

            // Primero agregamos todos los mensajes pasados del modelo como hints anteriores
            if (llmAnswers != null && !llmAnswers.isEmpty()) {
                int i = 1;
                for (String answer : llmAnswers) {
                    contents.add(Map.of(
                            "role", "model",
                            PARTS, List.of(
                                    Map.of("text",  answer)
                            )
                    ));
                }
            }

            // Luego añadimos el mensaje actual del usuario, con aclaración de las pistas anteriores
            contents.add(Map.of(
                    "role", "user",
                    PARTS, List.of(
                            Map.of("text",
                                    "You are acting as a helpful assistant in a quiz game where players must guess the name of a city or a location based on an image. "
                                            + "Your role is to provide thoughtful and subtle hints to help guide the player toward the correct answer without revealing it directly. "
                                            + "Each hint should be informative but not too obvious — the goal is to challenge the player, not to give away the answer. "
                                            + "The user may ask in different languages, and you must always respond in the language specified in their question or selected via a language acronym. "
                                            + "If you have already provided hints for the same image before, make sure your new hint is different and offers a new angle or perspective. "
                                            + "You must only provide **one** hint per request. Avoid repeating earlier hints, and do not restate the question. "
                                            + "Keep your response focused and concise: no greetings, no unnecessary introductions — just the hint itself. "
                                            + "Do not say things like 'Here is your hint:' or 'I think it might be...' Just state the hint directly and naturally.\n\n"
                                            + "The above messages are hints you have already given for this question. Make sure your new hint is different from them and continuos the enumeration. "
                                            + "Now, based on the following question and image, provide a new hint:\n" + question)
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
