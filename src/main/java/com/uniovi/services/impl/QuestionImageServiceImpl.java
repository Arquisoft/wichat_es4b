package com.uniovi.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.uniovi.dto.QuestionImageDto;
import com.uniovi.entities.AnswerImage;
import com.uniovi.entities.Associations;
import com.uniovi.entities.Category;
import com.uniovi.entities.QuestionImage;
import com.uniovi.repositories.AnswerImageRepository;
import com.uniovi.repositories.QuestionImageRepository;
import com.uniovi.services.LlmService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class QuestionImageServiceImpl implements QuestionService<QuestionImage, QuestionImageDto> {

    public static final Integer SECONDS_PER_QUESTION = 60;

    private final QuestionImageRepository questionRepository;
    private final CategoryServiceImpl categoryService;
    private final AnswerServiceImageImpl answerService;
    private final AnswerImageRepository answerRepository;
    private final EntityManager entityManager;
    private final LlmService llmService;

    @Setter
    private QuestionImageGeneratorServiceImpl questionGeneratorService;

    private final Random random = new SecureRandom();

    public QuestionImageServiceImpl(QuestionImageRepository questionRepository, CategoryServiceImpl categoryService,
                                    AnswerServiceImageImpl answerService, AnswerImageRepository answerRepository,
                                    EntityManager entityManager, LlmService llmService) {
        this.questionRepository = questionRepository;
        this.categoryService = categoryService;
        this.answerService = answerService;
        this.answerRepository = answerRepository;
        this.entityManager = entityManager;
        this.llmService = llmService;
    }

    @Override
    public void addNewQuestion(QuestionImage questionImage) {
        questionRepository.save(questionImage);
    }

    @Override
    public QuestionImage addNewQuestion(QuestionImageDto questionImage) {
        Category category = categoryService.getCategoryByName(questionImage.getCategory().getName());
        if (category == null) {
            categoryService.addNewCategory(new Category(questionImage.getCategory().getName(), questionImage.getCategory().getDescription()));
            category = categoryService.getCategoryByName(questionImage.getCategory().getName());
        }

        List<AnswerImage> answersImage = new ArrayList<>();
        for (int i = 0; i < questionImage.getOptions().size(); i++) {
            AnswerImage a = new AnswerImage();
            a.setText(questionImage.getOptions().get(i).getText());
            a.setCorrect(questionImage.getOptions().get(i).isCorrect());
            answerService.addNewAnswer(a);
            answersImage.add(a);
        }

        QuestionImage q = new QuestionImage();
        q.setStatement(questionImage.getStatement());
        q.setLanguage(questionImage.getLanguage());
        Associations.QuestionsImageCategory.addCategory(q, category);
        Associations.QuestionImageAnswers.addAnswer(q, answersImage);
        q.setImageUrl(questionImage.getImageUrl());
        addNewQuestion(q);

        return q;
    }

    @Override
    public List<QuestionImage> getAllQuestions() {
        return new ArrayList<>(questionRepository.findAll());
    }

    @Override
    public Page<QuestionImage> getQuestions(Pageable pageable) {
        return questionRepository.findByLanguage(pageable, LocaleContextHolder.getLocale().getLanguage());
    }

    @Override
    public Optional<QuestionImage> getQuestion(Long id) {
        return questionRepository.findById(id);
    }

    @Override
    public List<QuestionImage> getRandomQuestions(int num) {
        List<QuestionImage> allQuestions = questionRepository.findAll().stream()
                .filter(question -> question.getLanguage().equals(LocaleContextHolder.getLocale().getLanguage())).toList();
        List<QuestionImage> res = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            int idx = random.nextInt(allQuestions.size());
            while (allQuestions.get(idx).hasEmptyOptions() || res.contains(allQuestions.get(idx))) {
                idx = random.nextInt(allQuestions.size());
            }
            res.add(allQuestions.get(idx));
        }
        return res;
    }

    @Override
    public boolean checkAnswer(Long idquestion, Long idanswer) {
        Optional<QuestionImage> q = questionRepository.findById(idquestion);
        return q.map(question -> question.getCorrectAnswer().getId().equals(idanswer)).orElse(false);
    }

    @Override
    public List<QuestionImage> getQuestionsByCategory(Pageable pageable, Category category, String lang) {
        return questionRepository.findByCategoryAndLanguage(pageable, category, lang).toList();
    }

    @Override
    public List<QuestionImage> getQuestionsByStatement(Pageable pageable, String statement, String lang) {
        return questionRepository.findByStatementAndLanguage(pageable, statement, lang).toList();
    }

    @Override
    public void updateQuestion(Long id, QuestionImageDto questionDto) {
        Optional<QuestionImage> q = questionRepository.findById(id);
        if (q.isPresent()) {
            entityManager.clear();
            QuestionImage question = q.get();
            question.setStatement(questionDto.getStatement());
            question.setLanguage(questionDto.getLanguage());
            Category category = categoryService.getCategoryByName(questionDto.getCategory().getName());
            if (category == null) {
                categoryService.addNewCategory(new Category(questionDto.getCategory().getName(), questionDto.getCategory().getDescription()));
                category = categoryService.getCategoryByName(questionDto.getCategory().getName());
            }

            Associations.QuestionsImageCategory.removeCategory(question, question.getCategory());

            for (int i = 0; i < questionDto.getOptions().size(); i++) {
                AnswerImage a = question.getOption(i);
                a.setText(questionDto.getOptions().get(i).getText());
                a.setCorrect(questionDto.getOptions().get(i).isCorrect());
            }

            Associations.QuestionsImageCategory.addCategory(question, category);
            questionRepository.save(question);
        }
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        Optional<QuestionImage> q = questionRepository.findById(id);
        if (q.isPresent()) {
            QuestionImage question = q.get();
            answerRepository.deleteAll(question.getOptions());
            Associations.QuestionImageAnswers.removeAnswer(question, question.getOptions());
            Associations.QuestionsImageCategory.removeCategory(question, question.getCategory());
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

    public String getHintForImageQuestion(QuestionImage question) {

        String llmHint = ("Hola, tengo esta imagen: <" + question.getImageUrl() + ">, estas opciones de respuesta: "
                + question.getOptions().toString() + ".\nLa respuesta correcta es: " + question.getCorrectAnswer()  +  ".\nY quiero que me respondas en el idioma de este acrónimo: " + question.getLanguage());
        // Llamar al servicio LLM para obtener la pista usando Gemini.
        return llmService.sendQuestionToLLM(llmHint);
    }

}
