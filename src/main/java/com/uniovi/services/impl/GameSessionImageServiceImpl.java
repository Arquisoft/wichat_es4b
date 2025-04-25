package com.uniovi.services.impl;

import com.uniovi.entities.Associations;
import com.uniovi.entities.GameSessionImage;
import com.uniovi.entities.Player;
import com.uniovi.entities.QuestionImage;
import com.uniovi.repositories.GameSessionImageRepository;
import com.uniovi.services.GameSessionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GameSessionImageServiceImpl implements GameSessionService<GameSessionImage> {

    public static final Integer NORMAL_GAME_QUESTION_NUM = 4;

    private final GameSessionImageRepository gameSessionRepository;
    private final QuestionImageServiceImpl questionService;
    private final MultiplayerSessionImageServiceImpl multiplayerSessionService;

    public GameSessionImageServiceImpl(GameSessionImageRepository gameSessionRepository, QuestionImageServiceImpl questionService, MultiplayerSessionImageServiceImpl multiplayerSessionService) {
        this.gameSessionRepository = gameSessionRepository;
        this.questionService = questionService;
        this.multiplayerSessionService = multiplayerSessionService;
    }

    @Override
    public List<GameSessionImage> getGameSessions() {
        return gameSessionRepository.findAll();
    }

    @Override
    public List<GameSessionImage> getGameSessionsByPlayer(Player player) {
        return gameSessionRepository.findAllByPlayer(player);
    }

    @Override
    public Page<Object[]> getGlobalRanking(Pageable pageable) {
        return gameSessionRepository.findTotalScoresByPlayer(pageable);
    }

    @Override
    public Page<GameSessionImage> getPlayerRanking(Pageable pageable, Player player) {
        return gameSessionRepository.findAllByPlayerOrderByScoreDesc(pageable, player);
    }

    @Override
    public GameSessionImage startNewGame(Player player) {
        return new GameSessionImage(player, questionService.getRandomQuestions(NORMAL_GAME_QUESTION_NUM));
    }

    @Override
    public GameSessionImage startNewMultiplayerGame(Player player, int code) {
        List<QuestionImage> qs = multiplayerSessionService.getQuestions(String.valueOf(code));
        if (qs == null) return null;

        GameSessionImage sess = new GameSessionImage(player, qs);
        sess.setMultiplayer(true);
        return sess;
    }

    @Override
    public void endGame(GameSessionImage gameSession) {
        Associations.PlayerGameSessionImage.addGameSession(gameSession.getPlayer(), gameSession);
        gameSession.setFinishTime(LocalDateTime.now());
        gameSessionRepository.save(gameSession);
    }
}
