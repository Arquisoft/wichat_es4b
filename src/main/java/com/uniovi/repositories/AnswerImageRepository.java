package com.uniovi.repositories;

import com.uniovi.entities.AnswerImage;
import com.uniovi.entities.QuestionImage;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AnswerImageRepository extends CrudRepository<AnswerImage, Long> {

    List<AnswerImage> findByQuestion(QuestionImage questionImage);
}
