package com.uniovi.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.uniovi.dto.QuestionBaseDto;
import com.uniovi.dto.QuestionDto;
import com.uniovi.entities.*;
import com.uniovi.repositories.AnswerRepository;
import com.uniovi.repositories.QuestionRepository;
import com.uniovi.services.AnswerService;
import com.uniovi.services.CategoryService;
import com.uniovi.services.QuestionGeneratorService;
import com.uniovi.services.QuestionService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.Setter;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
@Service
public class QuestionServiceImpl extends QuestionBaseServiceImpl<Question, QuestionDto> {

    public QuestionServiceImpl(QuestionRepository questionRepository, CategoryService categoryService,
                               AnswerService answerService, AnswerRepository answerRepository,
                               EntityManager entityManager) {
        super(questionRepository, categoryService, answerService, answerRepository, entityManager);
    }


    @Override
    protected Question createQuestionInstance(QuestionDto question, Category category, List<Answer> answers) {
        Question q = new Question();
        q.setStatement(question.getStatement());
        q.setLanguage(question.getLanguage());
        Associations.QuestionsCategory.addCategory(q, category);
        Associations.QuestionAnswers.addAnswer(q, answers);
        return q;
    }


    @Override
    protected void disAssociateCategory(Question question) {
        Associations.QuestionsCategory.removeCategory(question, question.getCategory());
    }

    @Override
    protected void associateCategory(Question question, Category category) {
        Associations.QuestionsCategory.addCategory(question, category);
    }

    @Override
    protected void disAssociateAnswers(Question question) {
        Associations.QuestionAnswers.removeAnswer(question, question.getOptions());
    }



}
