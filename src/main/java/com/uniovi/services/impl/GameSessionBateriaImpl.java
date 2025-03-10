package com.uniovi.services.impl;

import com.uniovi.entities.Associations;
import com.uniovi.entities.GameSession;
import com.uniovi.entities.GameSessionBateria;
import com.uniovi.entities.Player;
import com.uniovi.repositories.GameSessionBateriaRepository;
import com.uniovi.repositories.GameSessionRepository;
import com.uniovi.services.GameSessionBateriaService;
import com.uniovi.services.GameSessionService;
import com.uniovi.services.QuestionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GameSessionBateriaImpl implements GameSessionBateriaService {
    private final GameSessionBateriaRepository gameSessionRepository;
    private final QuestionService questionService;

    public GameSessionBateriaImpl(GameSessionBateriaRepository gameSessionRepository, QuestionService questionService) {
        this.gameSessionRepository = gameSessionRepository;
        this.questionService = questionService;
    }

    @Override
    public List<GameSessionBateria> getGameSessions() {
        return gameSessionRepository.findAll();
    }

    @Override
    public List<GameSessionBateria> getGameSessionsByPlayer(Player player) {
        return gameSessionRepository.findAllByPlayer(player);
    }

    @Override
    public Page<Object[]> getGlobalRanking(Pageable pageable) {
        return gameSessionRepository.findTotalScoresByPlayer(pageable);
    }

    @Override
    public Page<GameSessionBateria> getPlayerRanking(Pageable pageable, Player player) {
        return gameSessionRepository.findAllByPlayerOrderByScoreDesc(pageable, player);
    }

    @Override
    public GameSessionBateria startNewGame(Player player) {
        return new GameSessionBateria(player, questionService.getRandomQuestions(Integer.MAX_VALUE));
    }

    @Override
    public GameSessionBateria startNewMultiplayerGame(Player player, int code) {
        //NOT YET IMPLEMENTED
        return null;
    }

    @Override
    public void endGame(GameSessionBateria gameSession) {
        Associations.PlayerBateriaGameSession.addGameSession(gameSession.getPlayer(), gameSession);
        gameSession.setFinishTime(LocalDateTime.now());
        gameSessionRepository.save(gameSession);
    }
}
