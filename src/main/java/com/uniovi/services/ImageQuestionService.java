package com.uniovi.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.uniovi.dto.ImageQuestionDto;
import com.uniovi.entities.*;
import com.uniovi.repositories.AnswerRepository;
import com.uniovi.repositories.ImageQuestionRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.Setter;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;

@Service
public class ImageQuestionService {
    public static final int SECONDS_PER_QUESTION = 30;
    private final ImageQuestionRepository imageQuestionRepository;
    private final CategoryService categoryService;
    private final AnswerService answerService;
    private final AnswerRepository answerRepository;
    private final EntityManager entityManager;
    private final LlmService llmService;

    @Setter
    private QuestionGeneratorService questionGeneratorService;

    private final Random random = new SecureRandom();

    public ImageQuestionService(
            ImageQuestionRepository imageQuestionRepository,
            CategoryService categoryService,
            AnswerService answerService,
            AnswerRepository answerRepository,
            EntityManager entityManager, LlmService llmService) {
        this.imageQuestionRepository = imageQuestionRepository;
        this.categoryService = categoryService;
        this.answerService = answerService;
        this.answerRepository = answerRepository;
        this.entityManager = entityManager;
        this.llmService = llmService;
    }

    private Category getCategoryOrCreate(String categoryName, String description) {
        Category category = categoryService.getCategoryByName(categoryName);
        if (category == null) {
            category = new Category(categoryName, description);
            categoryService.addNewCategory(category);
        }
        return category;
    }

    private void validateImageQuestionDto(ImageQuestionDto imageQuestionDto) {
        if (imageQuestionDto == null) {
            throw new IllegalArgumentException("ImageQuestionDto cannot be null");
        }
        if (StringUtils.isEmpty(imageQuestionDto.getStatement())) {
            throw new IllegalArgumentException("Statement cannot be empty");
        }
        if (imageQuestionDto.getOptions() == null || imageQuestionDto.getOptions().isEmpty()) {
            throw new IllegalArgumentException("At least one option is required");
        }
        long correctAnswers = imageQuestionDto.getOptions().stream()
                .filter(option -> option.isCorrect())
                .count();

        if (correctAnswers != 1) {
            throw new IllegalArgumentException("One correct answer is required, just one");
        }
    }

    public ImageQuestion addNewImageQuestion(ImageQuestionDto imageQuestionDto) {
        // Validar el DTO
        validateImageQuestionDto(imageQuestionDto);

        // Primero, obtener o crear la categoría
        Category category = getCategoryOrCreate(imageQuestionDto.getCategory().getName(), imageQuestionDto.getCategory().getDescription());

        // Crear las respuestas
        List<Answer> answers = new ArrayList<>();
        for (int i = 0; i < imageQuestionDto.getOptions().size(); i++) {
            Answer answer = new Answer();
            answer.setText(imageQuestionDto.getOptions().get(i).getText());
            answer.setCorrect(imageQuestionDto.getOptions().get(i).isCorrect());
            answerService.addNewAnswer(answer);
            answers.add(answer);
        }

        // Crear la pregunta con imagen
        ImageQuestion imageQuestion = new ImageQuestion();
        imageQuestion.setStatement(imageQuestionDto.getStatement());
        imageQuestion.setLanguage(imageQuestionDto.getLanguage());
        imageQuestion.setImageUrl(imageQuestionDto.getImageUrl());
        Associations.ImageQuestionCategory.addCategory(imageQuestion, category);
        Associations.ImageQuestionAnswers.addAnswer(imageQuestion, answers);

        // Guardar la pregunta con imagen
        imageQuestionRepository.save(imageQuestion);
        return imageQuestion;
    }


    public List<ImageQuestion> getAllImageQuestions() {
        return new ArrayList<>(imageQuestionRepository.findAll());
    }

    public Page<ImageQuestion> getImageQuestions(Pageable pageable) {
        return imageQuestionRepository.findByLanguage(pageable, LocaleContextHolder.getLocale().getLanguage());
    }

    public Optional<ImageQuestion> getImageQuestion(Long id) {
        return imageQuestionRepository.findById(id);
    }

    public List<ImageQuestion> getRandomImageQuestions(int num) {
        List<ImageQuestion> allImageQuestions = imageQuestionRepository.findAll();
        Collections.shuffle(allImageQuestions); // Mezcla las preguntas
        return allImageQuestions.subList(0, Math.min(num, allImageQuestions.size()));
    }


    public boolean checkAnswer(Long imageQuestionId, Long answerId) {
        Optional<ImageQuestion> imageQuestion = imageQuestionRepository.findById(imageQuestionId);
        if (imageQuestion.isPresent()) {
            return imageQuestion.get().getCorrectAnswer().getId().equals(answerId);
        }
        return false;
    }

    public List<ImageQuestion> getImageQuestionsByCategory(Pageable pageable, Category category, String lang) {
        return imageQuestionRepository.findByCategoryAndLanguage(pageable, category, lang).toList();
    }

    public List<ImageQuestion> getImageQuestionsByStatement(Pageable pageable, String statement, String lang) {
        return imageQuestionRepository.findByStatementAndLanguage(pageable, statement, lang).toList();
    }

    public void updateImageQuestion(Long id, ImageQuestionDto imageQuestionDto) {
        // Validar el DTO
        validateImageQuestionDto(imageQuestionDto);

        Optional<ImageQuestion> iq = imageQuestionRepository.findById(id);
        if (iq.isPresent()) {
            entityManager.clear();
            ImageQuestion imageQuestion = iq.get();
            imageQuestion.setStatement(imageQuestionDto.getStatement());
            imageQuestion.setLanguage(imageQuestionDto.getLanguage());
            imageQuestion.setImageUrl(imageQuestionDto.getImageUrl());

            // Actualizar categoría
            Category category = getCategoryOrCreate(imageQuestionDto.getCategory().getName(), imageQuestionDto.getCategory().getDescription());
            Associations.ImageQuestionCategory.removeCategory(imageQuestion, imageQuestion.getCategory());
            Associations.ImageQuestionCategory.addCategory(imageQuestion, category);

            // Actualizar respuestas
            List<Answer> answers = new ArrayList<>();
            for (int i = 0; i < imageQuestionDto.getOptions().size(); i++) {
                Answer answer = imageQuestion.getOption(i);
                answer.setText(imageQuestionDto.getOptions().get(i).getText());
                answer.setCorrect(imageQuestionDto.getOptions().get(i).isCorrect());
                answers.add(answer);
            }
            Associations.ImageQuestionAnswers.addAnswer(imageQuestion, answers);

            imageQuestionRepository.save(imageQuestion);
        }
    }

    @Transactional
    public void deleteImageQuestion(Long id) {
        Optional<ImageQuestion> iq = imageQuestionRepository.findById(id);
        if (iq.isPresent()) {
            ImageQuestion imageQuestion = iq.get();
            answerRepository.deleteAll(imageQuestion.getOptions());
            Associations.ImageQuestionAnswers.removeAnswer(imageQuestion, imageQuestion.getOptions());
            Associations.ImageQuestionCategory.removeCategory(imageQuestion, imageQuestion.getCategory());
            imageQuestion.setCorrectAnswer(null);
            imageQuestionRepository.delete(imageQuestion);
        } else {
            throw new IllegalArgumentException("ImageQuestion with id " + id + " not found");
        }
    }

    public void deleteAllImageQuestions() throws IOException {
        imageQuestionRepository.deleteAll();
    }

    public void setJsonGenerator(JsonNode json) {
        questionGeneratorService.setJsonGeneration(json);
    }

    public JsonNode getJsonGenerator() {
        return questionGeneratorService.getJsonGeneration();
    }

    public String getHintForImageQuestion(String imageUrl, String playerQuestion) {


        // Formar la pregunta completa con el contexto de la imagen
        // README: Se puede pasar el objeto imageQuestion a este metodo para meterle más contexto sobre las distintas posibles respuestas
        //         o sobre la pregunta de la propia aplicación. Habría que probar para ver como da las pistas.
        String question = "Quiero que uses la imagen que te he pasado para darme una respuesta a " +
                "modo de pista de la siguiente pregunta relacionada con esa misma imagen, ¿" + playerQuestion +"?";

        // Llamar al servicio LLM para obtener la pista usando Gemini.
        return llmService.sendQuestionToLLM(question, imageUrl);
    }
}
