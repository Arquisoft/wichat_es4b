package com.uniovi.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniovi.components.generators.ImageQuestionGenerator;
import com.uniovi.components.generators.QuestionGenerator;
import com.uniovi.components.generators.QuestionGeneratorV2;
import com.uniovi.dto.ImageQuestionDto;
import com.uniovi.entities.Answer;
import com.uniovi.entities.Category;
import com.uniovi.entities.ImageQuestion;
import com.uniovi.entities.Question;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

@Service
public class ImageQuestionGeneratorService {

    private final ImageQuestionService imageQuestionService;
    public static final String JSON_FILE_PATH = "static/JSON/ImageQuestionTemplates.json";
    private final Deque<QuestionType> types = new ArrayDeque<>();
    private JsonNode json;
    private final Environment environment;
    private final Logger log = LoggerFactory.getLogger(ImageQuestionGeneratorService.class);
    private boolean started;

    public ImageQuestionGeneratorService(ImageQuestionService imageQuestionService, Environment environment) throws IOException {
        this.environment = environment;
        this.imageQuestionService = imageQuestionService;
        this.imageQuestionService.setQuestionGeneratorService(this);
        parseQuestionTypes();
        this.started = true;

    }

    private void parseQuestionTypes() throws IOException {
        if (json == null) {
            Resource resource = new ClassPathResource(JSON_FILE_PATH);
            ObjectMapper objectMapper = new ObjectMapper();
            json = objectMapper.readTree(resource.getInputStream());
        }

        JsonNode categories = json.findValue("categories");
        if (categories == null) {
            throw new IOException("El JSON de preguntas no contiene la clave 'categories'.");
        }

        for (JsonNode category : categories) {
            String categoryName = category.get("name").textValue();
            Category cat = new Category(categoryName);
            JsonNode questionsNode = category.findValue("questions");
            if (questionsNode != null) {
                for (JsonNode question : questionsNode) {
                    types.push(new QuestionType(question, cat));
                }
            }
        }
    }

    public void generateAllQuestions() throws IOException {
        started = true;
        resetGeneration();
    }

    @Transactional
    public void generateQuestions() throws IOException, InterruptedException {
        if (types.isEmpty()) {
            return;
        }

        if (started) {
            started = false;
            imageQuestionService.deleteAllImageQuestions();
        }

        if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> env.equalsIgnoreCase("test"))) {
            log.info("Test profile active, skipping sample data insertion");
            return;
        }

        ImageQuestionGenerator qgen = new ImageQuestionGenerator(json);

        while (!types.isEmpty()) {
            QuestionType type = types.pop();
            addImageQuestions(qgen,type);
            }
    }

    @Transactional
    public void generateTestQuestions() throws IOException, InterruptedException {
        if (types.isEmpty()) {
            log.warn("No hay más tipos de preguntas para generar.");
            return;
        }

        QuestionGenerator qgen = new QuestionGeneratorV2(json);
        QuestionType type = types.pop();
        type.getQuestion().has("image_placeholder");
    }

    @Transactional
    public void generateTestQuestions(String cat) {
        Answer a1 = new Answer("1", true);
        List<Answer> answers = List.of(a1, new Answer("2", false), new Answer("3", false), new Answer("4", false));
        ImageQuestion q = new ImageQuestion("Statement", answers, new Category(cat), "es", "No image");
        imageQuestionService.addNewImageQuestion(new ImageQuestionDto(q));
    }


    private void addImageQuestions(ImageQuestionGenerator qgen, QuestionType type) throws IOException, InterruptedException {
        List<ImageQuestionDto> questions;
        List<ImageQuestion> qsp = qgen.getImageQuestions(Question.SPANISH, type.getQuestion(), type.getCategory());
        questions = qsp.stream().map(ImageQuestionDto::new).toList();
        questions.forEach(imageQuestionService::addNewImageQuestion);

        List<ImageQuestion> qen = qgen.getImageQuestions(Question.ENGLISH, type.getQuestion(), type.getCategory());
        questions = qen.stream().map(ImageQuestionDto::new).toList();
        questions.forEach(imageQuestionService::addNewImageQuestion);

        List<ImageQuestion> qfr = qgen.getImageQuestions(Question.FRENCH, type.getQuestion(), type.getCategory());
        questions = qfr.stream().map(ImageQuestionDto::new).toList();
        questions.forEach(imageQuestionService::addNewImageQuestion);

        List<ImageQuestion> qDe = qgen.getImageQuestions(Question.DEUCH, type.getQuestion(), type.getCategory()); // Corregido
        questions = qDe.stream().map(ImageQuestionDto::new).toList();
        questions.forEach(imageQuestionService::addNewImageQuestion);
    }

    public void setJsonGeneration(JsonNode json) {
        this.json = json;
    }

    public void resetGeneration() throws IOException {
        types.clear();
        parseQuestionTypes();
    }

    public JsonNode getJsonGeneration() {
        return json;
    }

    @Getter
    @AllArgsConstructor
    private static class QuestionType {
        private final JsonNode question;
        private final Category category;
    }
}
