package com.uniovi.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniovi.components.generators.QuestionGenerator;
import com.uniovi.components.generators.QuestionGeneratorBaseImpl;
import com.uniovi.dto.QuestionBaseDto;
import com.uniovi.entities.Answer;
import com.uniovi.entities.Category;
import com.uniovi.entities.Language;
import com.uniovi.entities.QuestionBase;
import com.uniovi.services.impl.QuestionBaseServiceImpl;
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
import java.util.*;
@Service
public abstract class QuestionGeneratorServiceBase<T extends QuestionBase, P extends QuestionBaseDto> {

    protected final QuestionBaseServiceImpl<T, P> questionService;
    protected final Environment environment;
    protected final Logger log = LoggerFactory.getLogger(QuestionGeneratorServiceBase.class);
    protected final Deque<QuestionType> types = new ArrayDeque<>();
    protected JsonNode json;
    protected boolean started;

    public QuestionGeneratorServiceBase(QuestionBaseServiceImpl<T, P> questionService, Environment environment, String jsonFilePath) throws IOException {
        this.questionService = questionService;
        this.environment = environment;
        ((QuestionBaseServiceImpl<T, P>) questionService).setQuestionGeneratorService(this);
        loadJsonData(jsonFilePath);
        this.started = true;
    }

    /** Cargar JSON con las plantillas de preguntas */
    private void loadJsonData(String jsonFilePath) throws IOException {
        if (json == null) {
            Resource resource = new ClassPathResource(jsonFilePath);
            ObjectMapper objectMapper = new ObjectMapper();
            json = objectMapper.readTree(resource.getInputStream());
        }
        parseQuestionTypes();
    }

    /** Parsea las categorías y preguntas del JSON */
    private void parseQuestionTypes() {
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

        // Crear el generador de preguntas basado en la nueva estructura
        QuestionGeneratorBaseImpl<T> qgen = createQuestionGenerator(json);

        do {
            QuestionType type = types.pop();
            processQuestionGeneration(qgen, type);
        } while (!types.isEmpty());
    }

    /** Método abstracto para que cada implementación use su propio generador */
    protected abstract QuestionGeneratorBaseImpl<T> createQuestionGenerator(JsonNode json);

    /** Procesa la generación de preguntas en diferentes idiomas */
    protected void processQuestionGeneration(QuestionGeneratorBaseImpl<T> qgen, QuestionType type) {
        List<P> questions;

        for (Language lang : Language.values()) {
            List<T> generatedQuestions = generateQuestions(lang.getCode(), type.getQuestion(), type.getCategory(), qgen);
            questions = generatedQuestions.stream().map(this::convertToDto).toList();
            questions.forEach(questionService::addNewQuestionDto);
        }
    }

    /** Genera las preguntas utilizando el generador específico */
    private List<T> generateQuestions(String language, JsonNode question, Category category, QuestionGeneratorBaseImpl<T> qgen) {
        try {
            return qgen.getQuestions(language, question, category);
        } catch (IOException | InterruptedException e) {
            log.error("Error generating questions for language: {}", language, e);
            return new ArrayList<>();
        }
    }

    @Transactional
    public void generateTestQuestions() throws IOException, InterruptedException {
        QuestionGeneratorBaseImpl<T> qgen = createQuestionGenerator(json);
        QuestionType type = types.pop();
        processQuestionGeneration(qgen, type);
    }

    @Transactional
    public void generateTestQuestions(String cat) {
        Answer a1 = new Answer("1", true);
        List<Answer> answers = List.of(a1, new Answer("2", false), new Answer("3", false), new Answer("4", false));
        T q = createTestQuestion(answers,a1,cat);
        questionService.addNewQuestionDto(convertToDto(q));
    }

    public void setJsonGeneration(JsonNode json) {
        this.json = json;
    }

    public void resetGeneration() {
        types.clear();
        parseQuestionTypes();
    }

    public JsonNode getJsonGeneration() {
        return json;
    }

    /** Conversión de entidad a DTO */
    protected abstract P convertToDto(T question);

    protected abstract T createTestQuestion(List<Answer> answers, Answer a1, String cat);


    @Getter
    @AllArgsConstructor
    protected static class QuestionType {
        private final JsonNode question;
        private final Category category;
    }
}
