package com.uniovi.services;

import com.uniovi.entities.Answer;
import com.uniovi.entities.Question;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface AnswerService {

    /**
     * Add a new answer to the database
     *
     * @param answer Question to be added
     */
    void addNewAnswer(Answer answer);

    /**
     * Get all the answers for a question
     *
     * @return A list with all the answers for a question
     */
    List<Answer> getAnswersPerQuestion(Question question);

    /**
     * Get an answer by its id
     *
     * @param id The id of the answer
     * @return The answer with the given id
     */
    Optional<Answer> getAnswer(Long id);
}
