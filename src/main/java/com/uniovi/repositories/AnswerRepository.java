package com.uniovi.repositories;

import com.uniovi.entities.Answer;
import com.uniovi.entities.Question;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AnswerRepository extends CrudRepository<Answer, Long> {

    List<Answer> findByQuestion(Question question);
}
