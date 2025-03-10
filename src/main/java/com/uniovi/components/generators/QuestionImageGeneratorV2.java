package com.uniovi.components.generators;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniovi.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuestionImageGeneratorV2 implements QuestionImageGenerator{
    private final JsonNode jsonNode;
    private final String languagePlaceholder;
    private final String questionImagePlaceholder;
    private final String answerPlaceholder;
    private String language;

    private final Random random = new SecureRandom();
    private Logger logger = LoggerFactory.getLogger(QuestionImageGeneratorV2.class);

    public QuestionImageGeneratorV2(JsonNode jsonNode) {
        this.jsonNode = jsonNode;
        this.languagePlaceholder = jsonNode.get("language_placeholder").textValue();
        this.questionImagePlaceholder = jsonNode.get("question_placeholder").textValue();
        this.answerPlaceholder = jsonNode.get("answer_placeholder").textValue();
    }

    @Override
    public List<QuestionImage> getQuestions(String language) throws IOException, InterruptedException {
        this.language = language;
        List<QuestionImage> questionsImage = new ArrayList<>();
        JsonNode categories = jsonNode.findValue("categories");
        for(JsonNode category : categories){
            String categoryName = category.get("name").textValue();
            CategoryImage cat = new CategoryImage(categoryName);
            JsonNode questionsImageNode = category.findValue("questions");
            for(JsonNode questionImage : questionsImageNode){
                questionsImage.addAll(this.generateQuestionImage(questionImage, cat));
            }
        }
        return questionsImage;
    }



    @Override
    public List<QuestionImage> getQuestions(String language, JsonNode questionImage, CategoryImage cat) throws IOException, InterruptedException {
        this.language = language;
        return this.generateQuestionImage(questionImage, cat);
    }

    private List<QuestionImage> generateQuestionImage(JsonNode questionImage, CategoryImage cat) throws IOException, InterruptedException {
        // Get the SPARQL query from the JSON
        String query = questionImage.get("sparqlQuery").textValue();

        // Get the question and answer words from the JSON
        String questionImageLabel = questionImage.get("question").textValue();
        String answerLabel= questionImage.get("answer").textValue();
        String imageLabel = questionImage.get("image").textValue();

        // Replace the placeholders in the query with the actual values
        query = query.replace(languagePlaceholder, language).
                replace(questionImagePlaceholder, questionImageLabel).
                replace(answerPlaceholder, answerLabel);

        // Execute the query and get the results
        JsonNode results = null;
        boolean pass = false;
        do {
            try {
                results = getQueryResult(query);
                pass = true;
            } catch (Exception e) {
            }
        }while (!pass);
        List<QuestionImage> questionsImage = new ArrayList<>();

        // Prepare the statement base based on the language
        String statement = this.prepareStatement(questionImage);

        for (JsonNode result : results) {
            // Generate the correct answer
            String correctAnswer = result.path(answerLabel).path("value").asText();
            AnswerImage correct = new AnswerImage(correctAnswer, true);

            // Generate the options
            List<AnswerImage> options = this.generateOptions(results, correctAnswer, answerLabel);
            options.add(correct);

            if (statement != null) {
                // Generate the question statement
                String questionImageStatement = statement.replace(questionImagePlaceholder, result.path(questionImageLabel).path("value").asText());
                String imageUrl = result.path(imageLabel).path("value").asText();
                // Generate the question
                QuestionImage q = new QuestionImage(questionImageStatement, options, correct, cat, language, imageUrl);

                // Add the question to the list
                questionsImage.add(q);
            }
        }
        return questionsImage;
    }

    private List<AnswerImage> generateOptions(JsonNode results, String correctAnswer, String answerLabel) {
        List<AnswerImage> options = new ArrayList<>();
        List<String> usedOptions = new ArrayList<>();
        int size = results.size();
        int tries = 0;

       while (options.size() < 3 && tries < 10) {
            int randomIdx = random.nextInt(size);
            String option = results.get(randomIdx).path(answerLabel).path("value").asText();
            if (!option.equals(correctAnswer) && !usedOptions.contains(option) ) {
                usedOptions.add(option);
                options.add(new AnswerImage(option, false));
            }
            tries++;
        }
        return options;
    }

    /**
     * Generates a statement based on the language of the question
     * @param questionImage The question node
     * @return The statement in the language of the question or null if the language is not found
     */
    private String prepareStatement(JsonNode questionImage) {
        JsonNode statementNode = questionImage.findValue("statements");
        for (JsonNode statement : statementNode) {
            if (statement.get("language").textValue().equals(language)) {
                return statement.get("statement").textValue();
            }
        }
        return null;
    }

    private JsonNode getQueryResult(String query) throws IOException, InterruptedException {
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
