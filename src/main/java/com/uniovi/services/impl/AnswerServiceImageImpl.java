package com.uniovi.services.impl;

import com.uniovi.entities.AnswerImage;
import com.uniovi.entities.QuestionImage;
import com.uniovi.repositories.AnswerImageRepository;
import com.uniovi.services.AnswerService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AnswerServiceImageImpl implements AnswerService<AnswerImage, QuestionImage> {

    private final AnswerImageRepository answerRepository;

    public AnswerServiceImageImpl(AnswerImageRepository answerRepository) {
        this.answerRepository = answerRepository;
    }

    @Override
    public void addNewAnswer(AnswerImage answerImage) {
        answerRepository.save(answerImage);
    }

    @Override
    public List<AnswerImage> getAnswersPerQuestion(QuestionImage question) {
        return answerRepository.findByQuestion(question);
    }

    @Override
    public Optional<AnswerImage> getAnswer(Long id) {
        return answerRepository.findById(id);
    }
}
