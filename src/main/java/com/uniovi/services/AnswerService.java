package com.uniovi.services;

import com.uniovi.entities.abstracts.AbstractAnswer;
import com.uniovi.entities.abstracts.AbstractQuestion;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface AnswerService<T extends AbstractAnswer, C extends AbstractQuestion<?>> {

    /**
     * Add a new answer to the database
     *
     * @param answer Question to be added
     */
    void addNewAnswer(T answer);

    /**
     * Get all the answers for a question
     *
     * @return A list with all the answers for a question
     */
    List<T> getAnswersPerQuestion(C question);

    /**
     * Get an answer by its id
     *
     * @param id The id of the answer
     * @return The answer with the given id
     */
    Optional<T> getAnswer(Long id);
}
