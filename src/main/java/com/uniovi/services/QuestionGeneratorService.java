package com.uniovi.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniovi.components.generators.QuestionGenerator;
import com.uniovi.components.generators.QuestionGeneratorV2;
import com.uniovi.dto.QuestionDto;
import com.uniovi.entities.Answer;
import com.uniovi.entities.Category;
import com.uniovi.entities.Question;
import com.uniovi.services.impl.QuestionServiceImpl;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

@Service
public class QuestionGeneratorService {

    private final QuestionService questionService;

    public static final String JSON_FILE_PATH = "static/JSON/QuestionTemplates.json";

    private Deque<QuestionType> types = new ArrayDeque<>();

    private JsonNode json;

    private Environment environment;

    private final Logger log = LoggerFactory.getLogger(QuestionGeneratorService.class);

    private boolean started;

    public QuestionGeneratorService(QuestionService questionService, Environment environment) throws IOException {
        this.questionService = questionService;
        this.environment = environment;
        ((QuestionServiceImpl)questionService).setQuestionGeneratorService(this);
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
            JsonNode questionsNode = category.findValue("questions");
            for (JsonNode question : questionsNode) {
                types.push(new QuestionType(question, cat));
            }
        }
    }

    @Scheduled(fixedRate = 86400000, initialDelay = 86400000)
    public void generateAllQuestions() throws IOException {
        started = true;
        resetGeneration();
    }

    @Scheduled(fixedRate = 150000)
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

        QuestionGenerator qgen = new QuestionGeneratorV2(json);
        QuestionType type = types.pop();
        List<QuestionDto> questions;

        List<Question> qsp = qgen.getQuestions(Question.SPANISH, type.getQuestion(), type.getCategory());
        questions = qsp.stream().map(QuestionDto::new).toList();
        questions.forEach(questionService::addNewQuestion);

        List<Question> qen = qgen.getQuestions(Question.ENGLISH,  type.getQuestion(), type.getCategory());
        questions = qen.stream().map(QuestionDto::new).toList();
        questions.forEach(questionService::addNewQuestion);

        List<Question> qfr = qgen.getQuestions(Question.FRENCH,  type.getQuestion(), type.getCategory());
        questions = qfr.stream().map(QuestionDto::new).toList();
        questions.forEach(questionService::addNewQuestion);

        List<Question> qDe = qgen.getQuestions(Question.DEUCH,  type.getQuestion(), type.getCategory());
        questions = qDe.stream().map(QuestionDto::new).toList();
        questions.forEach(questionService::addNewQuestion);
    }

    @Transactional
    public void generateTestQuestions() throws IOException, InterruptedException {
        QuestionGenerator qgen = new QuestionGeneratorV2(json);
        QuestionType type = types.pop();
        List<QuestionDto> questions;

        List<Question> qsp = qgen.getQuestions(Question.SPANISH, type.getQuestion(), type.getCategory());
        questions = qsp.stream().map(QuestionDto::new).toList();
        questions.forEach(questionService::addNewQuestion);
    }

    @Transactional
    public void generateTestQuestions(String cat) {
        Answer a1 = new Answer("1", true);
        List<Answer> answers = List.of(a1, new Answer("2", false), new Answer("3", false), new Answer("4", false));
        Question q = new Question("Statement", answers, a1, new Category(cat), "es");
        questionService.addNewQuestion(new QuestionDto(q));
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
