package com.uniovi.components.generators;

import com.fasterxml.jackson.databind.JsonNode;
import com.uniovi.entities.AnswerImage;
import com.uniovi.entities.Category;
import com.uniovi.entities.QuestionImage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class QuestionImageGeneratorImpl extends AbstractQuestionGenerator<QuestionImage> {

    private Logger logger = LoggerFactory.getLogger(QuestionImageGeneratorImpl.class);

    public QuestionImageGeneratorImpl(JsonNode jsonNode) {
        super(jsonNode);

    }

    @Override
    protected List<QuestionImage> generateQuestion(JsonNode question, Category cat) throws IOException, InterruptedException {
        // Get the SPARQL query from the JSON
        String query = question.get("sparqlQuery").textValue();

        // Get the question and answer words from the JSON
        String questionImageLabel = question.get("question").textValue();
        String answerLabel= question.get("answer").textValue();
        String imageLabel = question.get("image").textValue();

        // Replace the placeholders in the query with the actual values
        query = query.replace(languagePlaceholder, language).
                replace(questionPlaceholder, questionImageLabel).
                replace(answerPlaceholder, answerLabel);

        // Execute the query and get the results
        List<QuestionImage> questionsImage = new ArrayList<>();
        boolean pass = false;
        List<String[]> results = null;

        do {
            try {
                results = getQueryResultL();
                pass = true;
            } catch (Exception e) {
                e.printStackTrace(); // Agregamos logging para depuración
            }
        } while (!pass);

// Verificamos que haya resultados antes de procesarlos
        if (results != null && !results.isEmpty()) {
            // Preparamos la declaración base según el idioma
            String statement = this.prepareStatement(question);

            for (String[] result : results) {
                String correctAnswer = result[0];
                AnswerImage correct = new AnswerImage(correctAnswer, true);

                // Generamos las opciones de respuesta
                List<AnswerImage> options = this.generateOptionsL(results, correctAnswer, questionImageLabel);
                options.add(correct); // Añadimos la respuesta correcta

                if (statement != null) {
                    String questionStatement = statement.replace(questionPlaceholder, correctAnswer);
                    String imageUrl = result[1];

                    // Creamos la pregunta
                    QuestionImage q = new QuestionImage(questionStatement, options, correct, cat, language, imageUrl);

                    // Agregamos la pregunta a la lista
                    questionsImage.add(q);
                }
            }
        }
        return questionsImage;
    }

    // Método para obtener las opciones
    protected List<AnswerImage> generateOptionsL(List<String[]> results, String correctAnswer, String answerLabel) {
        List<AnswerImage> options = new ArrayList<>();
        List<String> usedOptions = new ArrayList<>();
        int size = results.size();
        int tries = 0;

        while (options.size() < 3 && tries < 10) {
            int randomIdx = random.nextInt(size);
            String option = results.get(randomIdx)[0];
            if (!option.equals(correctAnswer) && !usedOptions.contains(option)) {
                usedOptions.add(option);
                options.add(new AnswerImage(option, false));
            }
            tries++;
        }
        return options;
    }

    @Override
    protected JsonNode getQueryResult(String query) throws IOException, InterruptedException {
        return null;
    }


    public static List<String[]> getQueryResultL() {
        String ENDPOINT = "https://query.wikidata.org/sparql";

        String QUERY = """
                SELECT DISTINCT ?capitalLabel ?image WHERE {
                                    ?country wdt:P31 wd:Q6256;  # Es un país
                                             wdt:P36 ?capital;  # Tiene una capital
                                             wdt:P30 ?continent. # Pertenece a un continente
                                    FILTER(?continent IN (wd:Q46, wd:Q49)) # Solo Europa y América
                                    OPTIONAL {\s
                                      ?capital wdt:P18 ?image. # Imagen representativa de la capital
                                    }
                                    SERVICE wikibase:label { bd:serviceParam wikibase:language "es". }
                                  } LIMIT 50
        """;
        List<String[]> resultados = new ArrayList<>();
        try {
            String urlStr = ENDPOINT + "?query=" + URLEncoder.encode(QUERY, "UTF-8") + "&format=json";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/sparql-results+json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Error HTTP: " + conn.getResponseCode());
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            conn.disconnect();

            JSONObject jsonObject = new JSONObject(response.toString());
            JSONArray bindings = jsonObject.getJSONObject("results").getJSONArray("bindings");

            for (int i = 0; i < bindings.length(); i++) {
                JSONObject item = bindings.getJSONObject(i);
                String capital = item.getJSONObject("capitalLabel").getString("value");
                String imagen = item.has("image") ? item.getJSONObject("image").getString("value") : "No disponible";
                resultados.add(new String[]{capital, imagen});
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultados;
    }

//    private JsonNode getQueryResult(String query) throws IOException, InterruptedException {
//        logger.info("Query: {}", query);
//        JsonNode resultsNode;
//        HttpResponse<String> response;
//        try (HttpClient client = HttpClient.newHttpClient()) {
//            String endpointUrl = "https://query.wikidata.org/sparql?query=" +
//                    URLEncoder.encode(query, StandardCharsets.UTF_8) +
//                    "&format=json";
//
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(endpointUrl))
//                    .header("Accept", "application/json")
//                    .build();
//
//            response = client.send(request, HttpResponse.BodyHandlers.ofString());
//        }
//
//        // Process the JSON response using Jackson ObjectMapper
//        ObjectMapper objectMapper = new ObjectMapper();
//        JsonNode jsonResponse =objectMapper.readTree(response.body());
//
//        // Access the data from the JSON response
//        resultsNode = jsonResponse.path("results").path("bindings");
//        return resultsNode;
//    }
}
