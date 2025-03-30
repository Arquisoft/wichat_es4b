package com.uniovi.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.uniovi.dto.QuestionBaseDto;
import com.uniovi.dto.QuestionDto;
import com.uniovi.entities.*;
import com.uniovi.repositories.AnswerRepository;
import com.uniovi.repositories.QuestionBaseRepository;
import com.uniovi.services.AnswerService;
import com.uniovi.services.CategoryService;
import com.uniovi.services.QuestionGeneratorServiceBase;
import com.uniovi.services.QuestionService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.Setter;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;
@Service
public abstract class QuestionBaseServiceImpl<T extends QuestionBase, P extends QuestionBaseDto> implements QuestionService<T, P> {

    protected final QuestionBaseRepository<T> questionRepository;
    protected final CategoryService categoryService;
    protected final AnswerService answerService;
    protected final AnswerRepository answerRepository;
    protected final EntityManager entityManager;

    @Setter
    protected QuestionGeneratorServiceBase<T,P> questionGeneratorService;

    protected final Random random = new SecureRandom();

    public QuestionBaseServiceImpl(QuestionBaseRepository<T> questionRepository, CategoryService categoryService,
                                   AnswerService answerService, AnswerRepository answerRepository,
                                   EntityManager entityManager) {
        this.questionRepository = questionRepository;
        this.categoryService = categoryService;
        this.answerService = answerService;
        this.answerRepository = answerRepository;
        this.entityManager = entityManager;
    }

    @Override
    public void addNewQuestion(T question) {
        questionRepository.save(question);
    }

    @Override
    public List<T> getAllQuestions() {
        return new ArrayList<>(questionRepository.findAll());
    }

    @Override
    public Page<T> getQuestions(Pageable pageable) {
        return questionRepository.findByLanguage(pageable, LocaleContextHolder.getLocale().getLanguage());
    }

    @Override
    public Optional<T> getQuestion(Long id) {
        return questionRepository.findById(id);
    }

    @Override
    public List<T> getRandomQuestions(int num) {
        List<T> allQuestions = questionRepository.findAll().stream()
                .filter(question -> question.getLanguage().equals(LocaleContextHolder.getLocale().getLanguage())).toList();
        List<T> res = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            int idx = random.nextInt(allQuestions.size());
            while (allQuestions.get(idx).hasEmptyOptions() || res.contains(allQuestions.get(idx))){
                idx = random.nextInt(allQuestions.size());
            }
            res.add(allQuestions.get(idx));
        }
        return res;
    }

    @Override
    public boolean checkAnswer(Long idquestion, Long idanswer) {
        Optional<T> q = questionRepository.findById(idquestion);
        return q.map(t -> t.getCorrectAnswer().getId().equals(idanswer)).orElse(false);
    }

    @Override
    public List<T> getQuestionsByCategory(Pageable pageable, Category category, String lang) {
        return questionRepository.findByCategoryAndLanguage(pageable, category, lang).toList();
    }

    @Override
    public List<T> getQuestionsByStatement(Pageable pageable, String statement, String lang) {
        return questionRepository.findByStatementAndLanguage(pageable, statement, lang).toList();
    }


    @Override
    public void updateQuestion(Long id, P questionDto) {
        Optional<T> q = questionRepository.findById(id);
        if (q.isPresent()) {
            entityManager.clear();
            T question = q.get();
            question.setStatement(questionDto.getStatement());
            question.setLanguage(questionDto.getLanguage());
            Category category = categoryService.getCategoryByName(questionDto.getCategory().getName());
            if (category == null) {
                categoryService.addNewCategory(new Category(questionDto.getCategory().getName(), questionDto.getCategory().getDescription()));
                category = categoryService.getCategoryByName(questionDto.getCategory().getName());
            }

            disAssociateCategory(question);

            for (int i = 0; i < questionDto.getOptions().size(); i++) {
                Answer a = question.getOption(i);
                a.setText(questionDto.getOptions().get(i).getText());
                a.setCorrect(questionDto.getOptions().get(i).isCorrect());
            }
            associateCategory(question, category);

            questionRepository.save(question);
        }
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        Optional<T> q = questionRepository.findById(id);
        if (q.isPresent()) {
            T question = q.get();
            answerRepository.deleteAll(question.getOptions());
            disAssociateAnswers(question);
            disAssociateCategory(question);

            q.get().setCorrectAnswer(null);
            questionRepository.delete(question);
        }
    }

    @Override
    public void deleteAllQuestions() throws IOException {
        questionGeneratorService.resetGeneration();
        questionRepository.deleteAll();
    }

    @Override
    public void setJsonGenerator(JsonNode json) {
        questionGeneratorService.setJsonGeneration(json);
    }

    @Override
    public JsonNode getJsonGenerator() {
        return questionGeneratorService.getJsonGeneration();
    }


    /** Método general para agregar preguntas desde DTO */
    @Override
    public T addNewQuestionDto(P questionDto) {
        Category category = getOrCreateCategory(questionDto);

        List<Answer> answers = createAnswers(questionDto);

        T question = createQuestionInstance(questionDto,category,answers);
        addNewQuestion(question);

        return question;
    }

    /** Método abstracto para crear una instancia de pregunta específica */
    protected abstract T createQuestionInstance(P questionDto, Category category, List<Answer> answers);

    protected abstract void disAssociateCategory(T question);

    protected abstract void associateCategory(T question, Category category);

    protected abstract void disAssociateAnswers(T question);

    /** Obtiene o crea una categoría */
    protected Category getOrCreateCategory(P questionDto) {
        Category category = categoryService.getCategoryByName(questionDto.getCategory().getName());
        if (category == null) {
            categoryService.addNewCategory(new Category(questionDto.getCategory().getName(), questionDto.getCategory().getDescription()));
            category = categoryService.getCategoryByName(questionDto.getCategory().getName());
        }
        return category;
    }

    /** Crea y almacena las respuestas de una pregunta */
    protected List<Answer> createAnswers(P questionDto) {
        List<Answer> answers = new ArrayList<>();
        for (var option : questionDto.getOptions()) {
            Answer a = new Answer();
            a.setText(option.getText());
            a.setCorrect(option.isCorrect());
            answerService.addNewAnswer(a);
            answers.add(a);
        }
        return answers;
    }


}
