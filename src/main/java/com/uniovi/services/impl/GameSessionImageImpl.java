package com.uniovi.services.impl;

import com.uniovi.entities.*;
import com.uniovi.repositories.GameSessionImageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GameSessionImageImpl  {
    public static Integer NORMAL_GAME_QUESTION_NUM = 4;
    private final GameSessionImageRepository gameSessionRepository;
    private final QuestionServiceImageImpl questionService;
    private final MultiplayerSessionImageImpl multiplayerSessionService;

    public GameSessionImageImpl(GameSessionImageRepository gameSessionRepository, QuestionServiceImageImpl questionService,
                                MultiplayerSessionImageImpl multiplayerSessionService) {
        this.gameSessionRepository = gameSessionRepository;
        this.questionService = questionService;
        this.multiplayerSessionService = multiplayerSessionService;
    }


    public List<GameSessionImage> getGameSessions() {
        return gameSessionRepository.findAll();
    }


    public List<GameSessionImage> getGameSessionsByPlayer(PlayerImage player) {
        return gameSessionRepository.findAllByPlayer(player);
    }

    public Page<Object[]> getGlobalRanking(Pageable pageable) {
        return gameSessionRepository.findTotalScoresByPlayer(pageable);
    }


    public Page<GameSessionImage> getPlayerRanking(Pageable pageable, PlayerImage player) {
        return gameSessionRepository.findAllByPlayerOrderByScoreDesc(pageable, player);
    }


    public GameSessionImage startNewGame(PlayerImage player) {
        return new GameSessionImage(player, questionService.getRandomQuestions(NORMAL_GAME_QUESTION_NUM));
    }


    public GameSessionImage startNewMultiplayerGame(PlayerImage player, int code) {
        List<QuestionImage> qs = multiplayerSessionService.getQuestions(String.valueOf(code));
        if (qs == null)
            return null;

        GameSessionImage sess = new GameSessionImage(player, qs);
        sess.setMultiplayer(true);
        return sess;
    }


    public void endGame(GameSessionImage gameSession) {
        Associations.PlayerGameSessionImage.addGameSession(gameSession.getPlayer(), gameSession);
        gameSession.setFinishTime(LocalDateTime.now());
        gameSessionRepository.save(gameSession);
    }
}
