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
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuestionGeneratorV2 implements QuestionGenerator{
    private final JsonNode jsonNode;
    private final String languagePlaceholder;
    private final String questionPlaceholder;
    private final String answerPlaceholder;
    private String language;

    private final Random random = new SecureRandom();
    private Logger logger = LoggerFactory.getLogger(QuestionGeneratorV2.class);

    public QuestionGeneratorV2(JsonNode jsonNode) {
        this.jsonNode = jsonNode;
        this.languagePlaceholder = jsonNode.get("language_placeholder").textValue();
        this.questionPlaceholder = jsonNode.get("question_placeholder").textValue();
        this.answerPlaceholder = jsonNode.get("answer_placeholder").textValue();
    }

    @Override
    public List<Question> getQuestions(String language) throws IOException, InterruptedException {
        this.language = language;
        List<Question> questions = new ArrayList<>();
        JsonNode categories = jsonNode.findValue("categories");
        for(JsonNode category : categories){
            String categoryName = category.get("name").textValue();
            Category cat = new Category(categoryName);
            JsonNode questionsNode = category.findValue("questions");
            for(JsonNode question : questionsNode){
                questions.addAll(this.generateQuestion(question, cat));
            }
        }
        return questions;
    }

    @Override
    public List<Question> getQuestions(String language, JsonNode question, Category cat) throws IOException, InterruptedException {
        this.language = language;
        return this.generateQuestion(question, cat);
    }

    private List<Question> generateQuestion(JsonNode question, Category cat) throws IOException, InterruptedException {
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

    private List<Answer> generateOptions(JsonNode results, String correctAnswer, String answerLabel) {
        List<Answer> options = new ArrayList<>();
        List<String> usedOptions = new ArrayList<>();
        int size = results.size();
        int tries = 0;

       while (options.size() < 3 && tries < 10) {
            int randomIdx = random.nextInt(size);
            String option = results.get(randomIdx).path(answerLabel).path("value").asText();
            if (!option.equals(correctAnswer) && !usedOptions.contains(option) ) {
                usedOptions.add(option);
                options.add(new Answer(option, false));
            }
            tries++;
        }
        return options;
    }

    /**
     * Generates a statement based on the language of the question
     * @param question The question node
     * @return The statement in the language of the question or null if the language is not found
     */
    private String prepareStatement(JsonNode question) {
        JsonNode statementNode = question.findValue("statements");
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
