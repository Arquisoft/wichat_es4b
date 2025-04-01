package com.uniovi.services.impl;

import com.uniovi.components.generators.QuestionGenerator;
import com.uniovi.components.generators.QuestionGeneratorV2;
import com.uniovi.dto.QuestionDto;
import com.uniovi.entities.Answer;
import com.uniovi.entities.Category;
import com.uniovi.entities.Language;
import com.uniovi.entities.Question;
import com.uniovi.services.abstracts.AbstractQuestionGeneratorService;
import jakarta.transaction.Transactional;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class QuestionGeneratorServiceImpl extends AbstractQuestionGeneratorService<QuestionServiceImpl> {

    public static final String JSON_FILE_PATH = "static/JSON/QuestionTemplates.json";

    public QuestionGeneratorServiceImpl(QuestionServiceImpl questionService, Environment environment) throws IOException {
        super(questionService, environment, JSON_FILE_PATH);
    }

    @Override
    protected void setQuestionGeneratorService() {
        questionService.setQuestionGeneratorServiceImpl(this);
    }

    @Override
    @Transactional
    protected void processQuestions() throws IOException, InterruptedException {
        QuestionGenerator qgen = new QuestionGeneratorV2(json);
        do {
            QuestionType type = types.pop();
            for (Language lang : List.of(Language.ES, Language.EN, Language.FR, Language.DE)) {
                List<Question> questions = qgen.getQuestions(lang.getCode(), type.getQuestion(), type.getCategory());
                questions.stream()
                        .map(QuestionDto::new)
                        .forEach(questionService::addNewQuestion);
            }
        } while (!types.isEmpty());
    }

    @Override
    @Transactional
    public void generateTestQuestions() throws IOException, InterruptedException {
        QuestionGenerator qgen = new QuestionGeneratorV2(json);
        QuestionType type = types.pop();
        List<QuestionDto> questions;

        List<Question> qsp = qgen.getQuestions(Language.ES.getCode(), type.getQuestion(), type.getCategory());
        questions = qsp.stream().map(QuestionDto::new).toList();
        questions.forEach(questionService::addNewQuestion);
    }

    @Override
    @Transactional
    public void generateTestQuestions(String cat) {
        Answer a1 = new Answer("1", true);
        List<Answer> answers = List.of(a1, new Answer("2", false), new Answer("3", false), new Answer("4", false));
        Question q = new Question("Statement", answers, a1, new Category(cat), Language.ES);
        questionService.addNewQuestion(new QuestionDto(q));
    }
}
