package com.uniovi.components.generators;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniovi.entities.Answer;
import com.uniovi.entities.Category;
import com.uniovi.entities.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class QuestionGeneratorImpl extends AbstractQuestionGenerator<Question> {


    private final Logger logger = LoggerFactory.getLogger(QuestionGeneratorImpl.class);

    public QuestionGeneratorImpl(JsonNode jsonNode) {
        super(jsonNode);
    }


    @Override
    protected List<Question> generateQuestion(JsonNode question, Category cat) throws InterruptedException {
        // Get the SPARQL query from the JSON
        String query = question.get("sparqlQuery").textValue();

        // Get the question and answer words from the JSON
        String questionLabel = question.get("question").textValue();
        String answerLabel= question.get("answer").textValue();

        // Replace the placeholders in the query with the actual values
        query = query.replace(languagePlaceholder, language).
                replace(questionPlaceholder, questionLabel).
                replace(answerPlaceholder, answerLabel);

        // Execute the query and get the results
        JsonNode results = null;
        boolean pass = false;
        do {
            try {
                results = getQueryResult(query);
                pass = true;
            }  catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Re-interrupt the thread
                throw e; // Rethrow the InterruptedException
            } catch (Exception ignored) {
            }
        }while (!pass);
        List<Question> questions = new ArrayList<>();

        // Prepare the statement base based on the language
        String statement = this.prepareStatement(question);

        for (JsonNode result : results) {
            // Generate the correct answer
            String correctAnswer = result.path(answerLabel).path("value").asText();
            Answer correct = new Answer(correctAnswer, true);

            // Generate the options
            List<Answer> options = this.generateOptions(results, correctAnswer, answerLabel);
            options.add(correct);

            if (statement != null) {
                // Generate the question statement
                String questionStatement = statement.replace(questionPlaceholder, result.path(questionLabel).path("value").asText());

                // Generate the question
                Question q = new Question(questionStatement, options, correct, cat, language);
                // Add the question to the list
                questions.add(q);
            }
        }
        return questions;
    }

    @Override
    protected JsonNode getQueryResult(String query) throws IOException, InterruptedException {
        logger.info("Query: {}", query);
        JsonNode resultsNode;
        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            String endpointUrl = "https://query.wikidata.org/sparql?query=" +
                    URLEncoder.encode(query, StandardCharsets.UTF_8) +
                    "&format=json";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpointUrl))
                    .header("Accept", "application/json")
                    .build();

            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        // Process the JSON response using Jackson ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonResponse =objectMapper.readTree(response.body());

        // Access the data from the JSON response
        resultsNode = jsonResponse.path("results").path("bindings");
        return resultsNode;
    }
}
