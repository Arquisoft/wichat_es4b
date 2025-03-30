package com.uniovi.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.uniovi.dto.QuestionDto;
import com.uniovi.dto.QuestionImageDto;
import com.uniovi.entities.*;
import com.uniovi.repositories.AnswerRepository;
import com.uniovi.repositories.QuestionImageRepository;
import com.uniovi.services.*;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.Setter;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class QuestionServiceImageImpl extends QuestionBaseServiceImpl<QuestionImage, QuestionImageDto> {

    private final LlmService llmService;



    public QuestionServiceImageImpl(QuestionImageRepository questionRepository, CategoryServiceImpl categoryService,
                                    AnswerServiceImpl answerService, AnswerRepository answerRepository,
                                    EntityManager entityManager, LlmService llmService) {
        super(questionRepository, categoryService, answerService, answerRepository, entityManager);
        this.llmService = llmService;
    }

    @Override
    protected QuestionImage createQuestionInstance(QuestionImageDto question, Category category, List<Answer> answers) {
        QuestionImage q = new QuestionImage();
        q.setStatement(question.getStatement());
        q.setLanguage(question.getLanguage());
        Associations.QuestionsCategory.addCategory(q, category);
        Associations.QuestionAnswers.addAnswer(q, answers);
        q.setImageUrl(question.getImageUrl());
        return q;
    }

    @Override
    protected void disAssociateCategory(QuestionImage question) {
        Associations.QuestionsCategory.removeCategory(question, question.getCategory());
    }

    @Override
    protected void associateCategory(QuestionImage question, Category category) {
        Associations.QuestionsCategory.addCategory(question, category);
    }

    @Override
    protected void disAssociateAnswers(QuestionImage question) {
        Associations.QuestionAnswers.removeAnswer(question, question.getOptions());
    }

    public String getHintForImageQuestion(QuestionImage question) {

        String llmHint =  ("Hola, tengo esta imagen: <" + question.getImageUrl() + ">, estas opciones de respuesta: "
                + question.getOptions().toString()+".\nY quiero que me respondas en el idioma de este acrónimo: " + question.getLanguage().toString());
        // Llamar al servicio LLM para obtener la pista usando Gemini.
        return llmService.sendQuestionToLLM(llmHint);
    }

}
