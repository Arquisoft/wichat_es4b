package com.uniovi.services.impl;

import com.uniovi.dto.QuestionBaseDto;
import com.uniovi.dto.QuestionImageDto;
import com.uniovi.entities.*;
import com.uniovi.repositories.GameSessionRepository;
import com.uniovi.services.GameSessionService;
import com.uniovi.services.MultiplayerSessionService;
import com.uniovi.services.QuestionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GameSessionImageImpl implements GameSessionService<QuestionImage,QuestionImageDto> {

    private final GameSessionRepository<QuestionImage> gameSessionRepository;
    private final QuestionServiceImageImpl questionService;
    private final MultiplayerSessionImpl<QuestionImage, QuestionImageDto> multiplayerSessionService;

    public GameSessionImageImpl(GameSessionRepository<QuestionImage> gameSessionRepository, QuestionServiceImageImpl questionService, MultiplayerSessionImpl<QuestionImage, QuestionImageDto> multiplayerSessionService) {
        this.gameSessionRepository = gameSessionRepository;
        this.questionService = questionService;
        this.multiplayerSessionService = multiplayerSessionService;
    }


    @Override
    public List<GameSession<QuestionImage>> getGameSessions() {
        return gameSessionRepository.findAll();
    }

    @Override
    public List<GameSession<QuestionImage>> getGameSessionsByPlayer(Player player) {
        return gameSessionRepository.findAllByPlayer(player);
    }

    public Page<Object[]> getGlobalRanking(Pageable pageable) {
        return gameSessionRepository.findTotalScoresByPlayer(pageable);
    }

    @Override
    public Page<GameSession<QuestionImage>> getPlayerRanking(Pageable pageable, Player player) {
        return gameSessionRepository.findAllByPlayerOrderByScoreDesc(pageable, player);
    }

    @Override
    public GameSession<QuestionImage> startNewGame(Player player) {
        return new GameSession<QuestionImage>(player, questionService.getRandomQuestions(NORMAL_GAME_QUESTION_NUM));
    }

    @Override
    public GameSession<QuestionImage> startNewMultiplayerGame(Player player, int code) {
        List<QuestionImage> qs = multiplayerSessionService.getQuestions(String.valueOf(code));
        if (qs == null)
            return null;

        GameSession<QuestionImage> sess = new GameSession<QuestionImage>(player, qs);
        sess.setMultiplayer(true);
        return sess;
    }

    @Override
    public void endGame(GameSession<QuestionImage> gameSession) {
        Associations.PlayerGameSession.addGameSession(gameSession.getPlayer(), gameSession);
        gameSession.setFinishTime(LocalDateTime.now());
        gameSessionRepository.save(gameSession);
    }
}
