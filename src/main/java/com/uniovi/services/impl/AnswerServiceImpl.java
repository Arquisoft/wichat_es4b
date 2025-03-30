package com.uniovi.services.impl;

import com.uniovi.entities.Answer;
import com.uniovi.entities.Question;
import com.uniovi.entities.QuestionBase;
import com.uniovi.repositories.AnswerRepository;
import com.uniovi.services.AnswerService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository answerRepository;

    public AnswerServiceImpl(AnswerRepository answerRepository) {
        this.answerRepository = answerRepository;
    }

    @Override
    public void addNewAnswer(Answer answer) {
        answerRepository.save(answer);
    }

    @Override
    public List<Answer> getAnswersPerQuestion(QuestionBase question) {
        return answerRepository.findByQuestion(question);
    }

    @Override
    public Optional<Answer> getAnswer(Long id) {
        return answerRepository.findById(id);
    }
}
