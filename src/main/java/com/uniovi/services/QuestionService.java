package com.uniovi.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.uniovi.dto.QuestionDto;
import com.uniovi.entities.Category;
import com.uniovi.entities.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public interface QuestionService {
    public static final Integer SECONDS_PER_QUESTION = 10;
    /**
     * Add a new question to the database
     *
     * @param question Question to be added
     */
    void addNewQuestion(Question question);

    /**
     * Add a new question to the database
     *
     * @param question Question to be added
     */
    Question addNewQuestion(QuestionDto question);

    /**
     * Get all the questions in the database
     *
     * @return A list with all the questions
     */
    List<Question> getAllQuestions();

    /**
     * Get a page with all the questions in the database
     *
     * @param pageable The page to get
     * @return A page with all the questions
     */
    Page<Question> getQuestions(Pageable pageable);

    /**
     * Get a question by its id
     *
     * @param id The id of the question
     * @return The question with the given id
     */
    Optional<Question> getQuestion(Long id);

    /**
     * Get a random question from any category
     *
     * @param num The number of questions to get
     * @return The questions selected
     */
    List<Question> getRandomQuestions(int num);

    /**
     * Check if the answer is correct
     * @param idquestion The id of the question
     * @param idanswer The id of the answer
     * @return True if the answer is correct, false otherwise
     */
    boolean checkAnswer(Long idquestion, Long idanswer);

    /**
     * Get the questions of a category
     * @param pageable The page to get
     * @param category The category of the questions
     * @param lang The language of the questions
     * @return The questions of the category
     */
    List<Question> getQuestionsByCategory(Pageable pageable, Category category, String lang);

    /**
     * Get the questions with a statement that contains the given string
     * @param pageable The page to get
     * @param statement The string to search
     * @param lang The language of the questions
     * @return The questions with the statement that contains the string
     */
    List<Question> getQuestionsByStatement(Pageable pageable, String statement, String lang);

    /**
     * Update a question
     * @param id The id of the question to update
     * @param questionDto The new data of the question
     */
    void updateQuestion(Long id, QuestionDto questionDto);

    /**
     * Delete a question
     * @param id The id of the question to delete
     */
    void deleteQuestion(Long id);

    /**
     * Delete all the questions
     */
    void deleteAllQuestions() throws IOException;

    /**
     * Set the json generator
     * @param json The json generator
     */
    void setJsonGenerator(JsonNode json);

    /**
     * Get the json generator
     * @return The json generator
     */
    JsonNode getJsonGenerator();
}
