package com.uniovi.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.uniovi.components.generators.QuestionGenerator;
import com.uniovi.components.generators.QuestionGeneratorBaseImpl;
import com.uniovi.components.generators.QuestionGeneratorImpl;
import com.uniovi.components.generators.QuestionImageGenerator;
import com.uniovi.dto.QuestionImageDto;
import com.uniovi.entities.*;
import com.uniovi.services.impl.QuestionServiceImageImpl;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class QuestionImageGeneratorService extends QuestionGeneratorServiceBase<QuestionImage, QuestionImageDto> {


    public static final String JSON_FILE_PATH = "static/JSON/QuestionImageTemplates.json";



    public QuestionImageGeneratorService(QuestionServiceImageImpl questionService, Environment environment) throws IOException {
        super(questionService,environment,JSON_FILE_PATH);
    }

    @Override
    protected QuestionGeneratorBaseImpl<QuestionImage> createQuestionGenerator(JsonNode json) {
        return new QuestionImageGenerator(json);
    }

    @Override
    protected QuestionImageDto convertToDto(QuestionImage question) {
        return new QuestionImageDto(question);
    }

    @Override
    protected QuestionImage createTestQuestion(List<Answer> answers, Answer a1, String cat) {
        return new QuestionImage("Statement", answers, a1, new Category(cat), Language.ES, "https://www.wikidata.org/wiki/Q487981#/media/File:Vista_de_Benidorm,_Espa%C3%B1a,_2014-07-02,_DD_67.JPG");
    }
}
