package com.uniovi.services.impl;

import com.uniovi.entities.QuestionImage;
import com.uniovi.repositories.MultiplayerSessionRepository;
import com.uniovi.repositories.PlayerRepository;
import com.uniovi.services.abstracts.AbstractMultiplayerSessionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MultiplayerSessionImageServiceImpl extends AbstractMultiplayerSessionService<QuestionImage> {

    private final QuestionImageServiceImpl questionService;

    public MultiplayerSessionImageServiceImpl(PlayerRepository playerRepository,
                                              MultiplayerSessionRepository multiplayerSessionRepository,
                                              QuestionImageServiceImpl questionService) {
        super(playerRepository, multiplayerSessionRepository);
        this.questionService = questionService;
    }

    @Override
    protected List<QuestionImage> getRandomQuestions() {
        return questionService.getRandomQuestions(GameSessionImageServiceImpl.NORMAL_GAME_QUESTION_NUM);
    }
}

