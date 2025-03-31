package com.uniovi.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniovi.components.generators.QuestionImageGenerator;
import com.uniovi.components.generators.QuestionImageGeneratorV2;
import com.uniovi.dto.QuestionImageDto;
import com.uniovi.entities.*;
import com.uniovi.services.impl.QuestionServiceImageImpl;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class QuestionImageGeneratorService {

    private final QuestionServiceImageImpl questionService;

    public static final String JSON_FILE_PATH = "static/JSON/QuestionImageTemplates.json";

    private final Deque<QuestionImageType> types = new ArrayDeque<>();

    private JsonNode json;

    private final Environment environment;

    private final Logger log = LoggerFactory.getLogger(QuestionImageGeneratorService.class);

    private boolean started;

    public QuestionImageGeneratorService(QuestionServiceImageImpl questionService, Environment environment) throws IOException {
        this.questionService = questionService;
        this.environment = environment;
        questionService.setQuestionGeneratorService(this);
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
        for (JsonNode category : categories) {
            String categoryName = category.get("name").textValue();
            Category cat = new Category(categoryName);
            JsonNode questionsImageNode = category.findValue("questions");
            for (JsonNode questionImage : questionsImageNode) {
                types.push(new QuestionImageType(questionImage, cat));
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
            questionService.deleteAllQuestions();
        }

        if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("test")))) {
            log.info("Test profile active, skipping sample data insertion");
            return;
        }

        QuestionImageGenerator qgen = new QuestionImageGeneratorV2(json);
        do{
            QuestionImageType type = types.pop();
            List<QuestionImageDto> questionsImage;

            List<QuestionImage> qsp = qgen.getQuestions(Language.ES.getCode(), type.getQuestionImage(), type.getCategory());
            questionsImage = qsp.stream().map(QuestionImageDto::new).toList();
            questionsImage.forEach(questionService::addNewQuestion);

            List<QuestionImage> qen = qgen.getQuestions(Language.EN.getCode(),  type.getQuestionImage(), type.getCategory());
            questionsImage = qen.stream().map(QuestionImageDto::new).toList();
            questionsImage.forEach(questionService::addNewQuestion);

            List<QuestionImage> qfr = qgen.getQuestions(Language.FR.getCode(),  type.getQuestionImage(), type.getCategory());
            questionsImage = qfr.stream().map(QuestionImageDto::new).toList();
            questionsImage.forEach(questionService::addNewQuestion);

            List<QuestionImage> qDe = qgen.getQuestions(Language.DE.getCode(),  type.getQuestionImage(), type.getCategory());
            questionsImage = qDe.stream().map(QuestionImageDto::new).toList();
            questionsImage.forEach(questionService::addNewQuestion);
        }while (!types.isEmpty());

    }

    @Transactional
    public void generateTestQuestions() throws IOException, InterruptedException {
        QuestionImageGenerator qgen = new QuestionImageGeneratorV2(json);
        QuestionImageType type = types.pop();
        List<QuestionImageDto> questions;

        List<QuestionImage> qsp = qgen.getQuestions(Language.ES.getCode(), type.getQuestionImage(), type.getCategory());
        questions = qsp.stream().map(QuestionImageDto::new).toList();
        questions.forEach(questionService::addNewQuestion);
    }

    @Transactional
    public void generateTestQuestions(String cat) {
        AnswerImage a1 = new AnswerImage("1", true);
        List<AnswerImage> answers = List.of(a1, new AnswerImage("2", false), new AnswerImage("3", false), new AnswerImage("4", false));
        QuestionImage q = new QuestionImage("Statement", answers, a1, new Category(cat), Language.ES, "URL");
        questionService.addNewQuestion(new QuestionImageDto(q));
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
    private static class QuestionImageType {
        private final JsonNode questionImage;
        private final Category category;
    }
}
