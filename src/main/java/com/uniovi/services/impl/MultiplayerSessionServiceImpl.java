package com.uniovi.services.impl;

import com.uniovi.entities.Question;
import com.uniovi.repositories.MultiplayerSessionRepository;
import com.uniovi.repositories.PlayerRepository;
import com.uniovi.services.GameSessionService;
import com.uniovi.services.abstracts.AbstractMultiplayerSessionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MultiplayerSessionServiceImpl extends AbstractMultiplayerSessionService<Question> {

    private final QuestionServiceImpl questionService;

    public MultiplayerSessionServiceImpl(PlayerRepository playerRepository,
                                         MultiplayerSessionRepository multiplayerSessionRepository,
                                         QuestionServiceImpl questionService) {
        super(playerRepository, multiplayerSessionRepository);
        this.questionService = questionService;
    }

    @Override
    protected List<Question> getRandomQuestions() {
        return questionService.getRandomQuestions(GameSessionService.NORMAL_GAME_QUESTION_NUM);
    }
}

