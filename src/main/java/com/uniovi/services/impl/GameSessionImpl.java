package com.uniovi.services.impl;

import com.uniovi.dto.QuestionBaseDto;
import com.uniovi.dto.QuestionDto;
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
public class GameSessionImpl implements GameSessionService<Question,QuestionDto> {

    private final GameSessionRepository<Question> gameSessionRepository;
    private final QuestionServiceImpl questionService;
    private final MultiplayerSessionService<Question, QuestionDto> multiplayerSessionService;

    public GameSessionImpl(GameSessionRepository<Question> gameSessionRepository, MultiplayerSessionService<Question,QuestionDto> multiplayerSessionService, QuestionServiceImpl questionService) {
        this.gameSessionRepository = gameSessionRepository;
        this.multiplayerSessionService = multiplayerSessionService;
        this.questionService = questionService;
    }


    @Override
    public List<GameSession<Question>> getGameSessions() {
        return gameSessionRepository.findAll();
    }

    @Override
    public List<GameSession<Question>> getGameSessionsByPlayer(Player player) {
        return gameSessionRepository.findAllByPlayer(player);
    }

    public Page<Object[]> getGlobalRanking(Pageable pageable) {
        return gameSessionRepository.findTotalScoresByPlayer(pageable);
    }

    @Override
    public Page<GameSession<Question>> getPlayerRanking(Pageable pageable, Player player) {
        return gameSessionRepository.findAllByPlayerOrderByScoreDesc(pageable, player);
    }

    @Override
    public GameSession<Question> startNewGame(Player player) {
        return new GameSession<Question>(player, questionService.getRandomQuestions(NORMAL_GAME_QUESTION_NUM));
    }

    @Override
    public GameSession<Question> startNewMultiplayerGame(Player player, int code) {
        List<Question> qs = multiplayerSessionService.getQuestions(String.valueOf(code));
        if (qs == null)
            return null;

        GameSession<Question> sess = new GameSession<Question>(player, qs);
        sess.setMultiplayer(true);
        return sess;
    }

    @Override
    public void endGame(GameSession<Question> gameSession) {
        Associations.PlayerGameSession.addGameSession(gameSession.getPlayer(), gameSession);
        gameSession.setFinishTime(LocalDateTime.now());
        gameSessionRepository.save(gameSession);
    }


}
