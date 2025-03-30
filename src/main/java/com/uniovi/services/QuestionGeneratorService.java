package com.uniovi.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.uniovi.components.generators.QuestionGenerator;
import com.uniovi.components.generators.QuestionGeneratorBaseImpl;
import com.uniovi.components.generators.QuestionGeneratorImpl;
import com.uniovi.dto.QuestionDto;
import com.uniovi.entities.*;
import com.uniovi.services.impl.QuestionServiceImpl;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class QuestionGeneratorService extends QuestionGeneratorServiceBase<Question, QuestionDto> {

    private static final String JSON_FILE_PATH = "static/JSON/QuestionTemplates.json";

    public QuestionGeneratorService(QuestionServiceImpl questionService, Environment environment) throws IOException {
        super(questionService, environment, JSON_FILE_PATH);
    }

    @Override
    protected QuestionGeneratorBaseImpl<Question> createQuestionGenerator(JsonNode json) {
        return new QuestionGeneratorImpl(json);
    }

    @Override
    protected QuestionDto convertToDto(Question question) {
        return new QuestionDto(question);
    }

    @Override
    protected Question createTestQuestion(List<Answer> answers, Answer a1, String cat) {
        return new Question("Statement", answers, a1, new Category(cat), Language.ES);
    }
}
