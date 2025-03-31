package com.uniovi.services.impl;

import com.uniovi.dto.QuestionBaseDto;
import com.uniovi.entities.Associations;
import com.uniovi.entities.GameSession;
import com.uniovi.entities.Player;
import com.uniovi.entities.QuestionBase;
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
public class GameSessionImpl<T extends QuestionBase, P extends QuestionBaseDto> implements GameSessionService<T,P> {

    private final GameSessionRepository<T> gameSessionRepository;
    private QuestionService<T, P> questionService;
    private MultiplayerSessionService<T,P> multiplayerSessionService;

    public GameSessionImpl(GameSessionRepository<T> gameSessionRepository) {
        this.gameSessionRepository = gameSessionRepository;
    }


    @Override
    public List<GameSession<T>> getGameSessions() {
        return gameSessionRepository.findAll();
    }

    @Override
    public List<GameSession<T>> getGameSessionsByPlayer(Player player) {
        return gameSessionRepository.findAllByPlayer(player);
    }

    public Page<Object[]> getGlobalRanking(Pageable pageable) {
        return gameSessionRepository.findTotalScoresByPlayer(pageable);
    }

    @Override
    public Page<GameSession<T>> getPlayerRanking(Pageable pageable, Player player) {
        return gameSessionRepository.findAllByPlayerOrderByScoreDesc(pageable, player);
    }

    @Override
    public GameSession<T> startNewGame(Player player) {
        return new GameSession<>(player, questionService.getRandomQuestions(NORMAL_GAME_QUESTION_NUM));
    }

    @Override
    public GameSession<T> startNewMultiplayerGame(Player player, int code) {
        List<T> qs = multiplayerSessionService.getQuestions(String.valueOf(code));
        if (qs == null)
            return null;

        GameSession<T> sess = new GameSession<T>(player, qs);
        sess.setMultiplayer(true);
        return sess;
    }

    @Override
    public void endGame(GameSession<T> gameSession) {
        Associations.PlayerGameSession.addGameSession(gameSession.getPlayer(), gameSession);
        gameSession.setFinishTime(LocalDateTime.now());
        gameSessionRepository.save(gameSession);
    }

    @Override
    public void setQuestionService(QuestionBaseServiceImpl<T, P> questionService) {
        this.questionService = questionService;
    }

    @Override
    public void setMultiplayerSessionService(MultiplayerSessionService<T, P> multiplayerSessionService) {
        this.multiplayerSessionService = multiplayerSessionService;
    }
}
