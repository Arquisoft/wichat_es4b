package com.uniovi.services.impl;

import com.uniovi.components.generators.QuestionImageGenerator;
import com.uniovi.components.generators.QuestionImageGeneratorV2;
import com.uniovi.dto.QuestionImageDto;
import com.uniovi.entities.AnswerImage;
import com.uniovi.entities.Category;
import com.uniovi.entities.Language;
import com.uniovi.entities.QuestionImage;
import com.uniovi.services.abstracts.AbstractQuestionGeneratorService;
import jakarta.transaction.Transactional;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class QuestionImageGeneratorServiceImpl extends AbstractQuestionGeneratorService<QuestionImageServiceImpl> {

    public static final String JSON_FILE_PATH = "static/JSON/QuestionImageTemplates.json";

    public QuestionImageGeneratorServiceImpl(QuestionImageServiceImpl questionService, Environment environment) throws IOException {
        super(questionService, environment, JSON_FILE_PATH);
    }

    @Override
    @Transactional
    protected void setQuestionGeneratorService() {
        questionService.setQuestionGeneratorService(this);
    }

    @Override
    @Transactional
    public void processQuestions() throws IOException, InterruptedException {
        QuestionImageGenerator qgen = new QuestionImageGeneratorV2(json);

        do {
            QuestionType type = types.pop();
            for (Language lang : List.of(Language.ES, Language.EN, Language.FR, Language.DE)) {
                List<QuestionImage> questions = qgen.getQuestions(lang.getCode(), type.getQuestion(), type.getCategory());
                questions.stream().map(QuestionImageDto::new).forEach(questionService::addNewQuestion);
            }
        } while (!types.isEmpty());
    }

    @Override
    @Transactional
    public void generateTestQuestions() throws IOException, InterruptedException {
        QuestionImageGenerator qgen = new QuestionImageGeneratorV2(json);
        QuestionType type = types.pop();
        List<QuestionImageDto> questions;

        List<QuestionImage> qsp = qgen.getQuestions(Language.ES.getCode(), type.getQuestion(), type.getCategory());
        questions = qsp.stream().map(QuestionImageDto::new).toList();
        questions.forEach(questionService::addNewQuestion);
    }

    @Override
    @Transactional
    public void generateTestQuestions(String cat) {
        AnswerImage a1 = new AnswerImage("1", true);
        List<AnswerImage> answers = List.of(a1, new AnswerImage("2", false), new AnswerImage("3", false), new AnswerImage("4", false));
        QuestionImage q = new QuestionImage("Statement", answers, a1, new Category(cat), Language.ES, "URL");
        questionService.addNewQuestion(new QuestionImageDto(q));
    }
}
