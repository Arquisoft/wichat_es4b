package com.uniovi.services.impl;

import com.uniovi.entities.*;
import com.uniovi.repositories.GameSessionRepository;
import com.uniovi.services.GameSessionService;
import com.uniovi.services.MultiplayerSessionService;
import com.uniovi.services.QuestionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class GameSessionImpl implements GameSessionService {

    private final GameSessionRepository gameSessionRepository;
    private final QuestionService questionService;
    private final MultiplayerSessionService multiplayerSessionService;

    public GameSessionImpl(GameSessionRepository gameSessionRepository, QuestionService questionService,
                           MultiplayerSessionService multiplayerSessionService) {
        this.gameSessionRepository = gameSessionRepository;
        this.questionService = questionService;
        this.multiplayerSessionService = multiplayerSessionService;
    }

    @Override
    public List<GameSession> getGameSessions() {
        return gameSessionRepository.findAll();
    }

    @Override
    public List<GameSession> getGameSessionsByPlayer(Player player) {
        return gameSessionRepository.findAllByPlayer(player);
    }

    public Page<Object[]> getGlobalRanking(Pageable pageable) {
        return gameSessionRepository.findTotalScoresByPlayer(pageable);
    }

    @Override
    public Page<GameSession> getPlayerRanking(Pageable pageable, Player player) {
        return gameSessionRepository.findAllByPlayerOrderByScoreDesc(pageable, player);
    }

    @Override
    public GameSession startNewGame(Player player) {
        return new GameSession(player, questionService.getRandomQuestions(NORMAL_GAME_QUESTION_NUM));
    }

    @Override
    public GameSession startNewMultiplayerGame(Player player, int code) {
        List<Question> qs = multiplayerSessionService.getQuestions(String.valueOf(code));
        if (qs == null)
            return null;

        GameSession sess = new GameSession(player, qs);
        sess.setMultiplayer(true);
        return sess;
    }

    @Override
    public void endGame(GameSession gameSession) {
        Associations.PlayerGameSession.addGameSession(gameSession.getPlayer(), gameSession);
        gameSession.setFinishTime(LocalDateTime.now());
        gameSessionRepository.save(gameSession);
    }
}
