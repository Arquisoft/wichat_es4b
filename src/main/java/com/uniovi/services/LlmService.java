package com.uniovi.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@Service
public class LlmService {

    // Definimos las configuraciones de los modelos de la misma forma que en el código Node.js.
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
    private static final String EMPATHY_API_URL = "https://empathyai.prod.empathy.co/v1/chat/completions";

    // Metodo genérico para enviar preguntas a los LLM.
    public String getLLMResponse(String question, String apiKey, String model) {
        RestTemplate restTemplate = new RestTemplate();

        String requestJson = null;
        String url = null;

        // Dependiendo del modelo, formateamos la solicitud y seleccionamos la URL correspondiente.
        if ("gemini".equals(model)) {
            url = GEMINI_API_URL + "?key=" + apiKey;
            requestJson = String.format("{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}", question);
        } else if ("empathy".equals(model)) {
            url = EMPATHY_API_URL;
            requestJson = String.format("{\"model\":\"qwen/Qwen2.5-Coder-7B-Instruct\",\"messages\":[{\"role\":\"system\",\"content\":\"You are a helpful assistant.\"},{\"role\":\"user\",\"content\":\"%s\"}]}", question);
        } else {
            return "Model not supported.";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        // Agregar el encabezado Authorization si es el modelo 'empathy'
        if ("empathy".equals(model)) {
            headers.set("Authorization", "Bearer " + apiKey);
        }

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        // Realizamos la solicitud POST.
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        // Procesamos la respuesta.
        if ("gemini".equals(model)) {
            return extractGeminiResponse(response.getBody());
        } else if ("empathy".equals(model)) {
            return extractEmpathyResponse(response.getBody());
        } else {
            return "Invalid model response.";
        }
    }

    // Procesamos la respuesta de Gemini (esto es específico para cada API).
    private String extractGeminiResponse(String responseBody) {
        // Aquí procesas la respuesta para extraer el contenido relevante (por ejemplo, usando JSON).
        if (responseBody.contains("candidates")) {
            return responseBody;  // Extrae lo que necesites de la respuesta JSON
        }
        return "No response from Gemini.";
    }

    // Procesamos la respuesta de Empathy (esto es específico para cada API).
    private String extractEmpathyResponse(String responseBody) {
        if (responseBody.contains("choices")) {
            // Extraemos el mensaje de la respuesta.
            return responseBody;  // Aquí procesas la respuesta JSON según sea necesario
        }
        return "No response from Empathy.";
    }
}
