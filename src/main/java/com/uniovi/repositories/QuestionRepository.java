package com.uniovi.repositories;

import com.uniovi.entities.Answer;
import com.uniovi.entities.Question;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends QuestionBaseRepository<Question> {

}
