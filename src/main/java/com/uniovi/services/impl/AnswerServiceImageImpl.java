package com.uniovi.services.impl;

import com.uniovi.entities.Answer;
import com.uniovi.entities.AnswerImage;
import com.uniovi.entities.Question;
import com.uniovi.entities.QuestionImage;
import com.uniovi.repositories.AnswerImageRepository;
import com.uniovi.repositories.AnswerRepository;
import com.uniovi.services.AnswerService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AnswerServiceImageImpl {

    private final AnswerImageRepository answerImageRepository;

    public AnswerServiceImageImpl(AnswerImageRepository answerRepository) {
        this.answerImageRepository = answerRepository;
    }


    public void addNewAnswer(AnswerImage answerImage) {
        answerImageRepository.save(answerImage);
    }


    public List<AnswerImage> getAnswersPerQuestion(QuestionImage questionImage) {
        return answerImageRepository.findByQuestion(questionImage);
    }


    public Optional<AnswerImage> getAnswer(Long id) {
        return answerImageRepository.findById(id);
    }
}
