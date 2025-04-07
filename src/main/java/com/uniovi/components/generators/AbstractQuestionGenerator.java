package com.uniovi.components.generators;

import com.fasterxml.jackson.databind.JsonNode;
import com.uniovi.entities.Answer;
import com.uniovi.entities.Category;
import com.uniovi.entities.abstracts.AbstractQuestion;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class AbstractQuestionGenerator<T extends AbstractQuestion> implements QuestionGenerator{
    protected final JsonNode jsonNode;
    protected final String languagePlaceholder;
    protected final String questionPlaceholder;
    protected final String answerPlaceholder;
    protected String language;

    protected final Random random = new SecureRandom();

    public AbstractQuestionGenerator(JsonNode jsonNode) {
        this.jsonNode = jsonNode;
        this.languagePlaceholder = jsonNode.get("language_placeholder").textValue();
        this.questionPlaceholder = jsonNode.get("question_placeholder").textValue();
        this.answerPlaceholder = jsonNode.get("answer_placeholder").textValue();
    }



    @Override
    public List<T> getQuestions(String language) throws IOException, InterruptedException {
        this.language = language;
        List<T> questions = new ArrayList<>();
        JsonNode categories = jsonNode.findValue("categories");
        for (JsonNode category : categories) {
            String categoryName = category.get("name").textValue();
            Category cat = new Category(categoryName);
            JsonNode questionsNode = category.findValue("questions");
            for (JsonNode question : questionsNode) {
                questions.addAll(this.generateQuestion(question, cat));
            }
        }
        return questions;
    }

    @Override
    public List<T> getQuestions(String language, JsonNode question, Category cat) throws IOException, InterruptedException {
        this.language = language;
        return this.generateQuestion(question, cat);
    }

    protected abstract List<T> generateQuestion(JsonNode question, Category cat) throws IOException, InterruptedException;

    // Método para obtener las opciones
    protected List<Answer> generateOptions(JsonNode results, String correctAnswer, String answerLabel) {
        List<Answer> options = new ArrayList<>();
        List<String> usedOptions = new ArrayList<>();
        int size = results.size();
        int tries = 0;

        while (options.size() < 3 && tries < 10) {
            int randomIdx = random.nextInt(size);
            String option = results.get(randomIdx).path(answerLabel).path("value").asText();
            if (!option.equals(correctAnswer) && !usedOptions.contains(option)) {
                usedOptions.add(option);
                options.add(new Answer(option, false));
            }
            tries++;
        }
        return options;
    }

    // Método para preparar la declaración
    protected String prepareStatement(JsonNode question) {
        JsonNode statementNode = question.findValue("statements");
        for (JsonNode statement : statementNode) {
            if (statement.get("language").textValue().equals(language)) {
                return statement.get("statement").textValue();
            }
        }
        return null;
    }

    // Método para obtener el resultado de la consulta SPARQL
    protected abstract JsonNode getQueryResult(String query) throws IOException, InterruptedException ;
}
