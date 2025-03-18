package com.uniovi.services.impl;

import com.uniovi.entities.*;
import com.uniovi.repositories.MultiplayerSessionImageRepository;
import com.uniovi.repositories.PlayerRepository;
import com.uniovi.services.GameSessionService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MultiplayerSessionImageImpl  {
    private final PlayerRepository playerRepository;
    private final MultiplayerSessionImageRepository multiplayerSessionRepository;
    private final QuestionServiceImageImpl questionService;

    private Map<String, List<QuestionImage>> multiplayerSessionQuestions = new HashMap<>();


    public MultiplayerSessionImageImpl(PlayerRepository playerRepository, MultiplayerSessionImageRepository multiplayerSessionRepository,
                                       QuestionServiceImageImpl questionService) {
        this.playerRepository = playerRepository;
        this.multiplayerSessionRepository = multiplayerSessionRepository;
        this.questionService = questionService;
    }


    @Transactional
    public Map<Player, Integer> getPlayersWithScores(int multiplayerCode) {
        MultiplayerSessionImage session = multiplayerSessionRepository.findByMultiplayerCode(String.valueOf(multiplayerCode));
        Map<Player, Integer> playerScores = session.getPlayerScores();

        // Ordenar los jugadores por puntuación de mayor a menor
        List<Player> sortedPlayers = playerScores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .toList();

        Map<Player, Integer> playersSorted = new HashMap<>();
        for (Player player : sortedPlayers) {
            playersSorted.put(player,playerScores.get(player));
        }
        return playersSorted;
    }


    public void multiCreate(String code, Long id) {
        Player p = playerRepository.findById(id).orElse(null);

        if (p != null) {
            multiplayerSessionRepository.save(new MultiplayerSessionImage(code, p));
            multiplayerSessionQuestions.put(code, questionService.getRandomQuestions(GameSessionService.NORMAL_GAME_QUESTION_NUM));
        }
    }


    @Transactional
    public void addToLobby(String code, Long id) {
        Player p = playerRepository.findById(id).orElse(null);

        if (p != null) {
            MultiplayerSessionImage ms = multiplayerSessionRepository.findByMultiplayerCode(code);
            ms.addPlayer(p);
            multiplayerSessionRepository.save(ms);
        }
    }


    @Transactional
    public void changeScore(String code, Long id, int score) {
        Player p = playerRepository.findById(id).orElse(null);

        if (p != null) {
            MultiplayerSessionImage ms = multiplayerSessionRepository.findByMultiplayerCode(code);
            ms.getPlayerScores().put(p, score);
            multiplayerSessionRepository.save(ms);
        }
    }


    public boolean existsCode(String code) {
        return multiplayerSessionRepository.findByMultiplayerCode(code) != null;
    }


    public List<QuestionImage> getQuestions(String code) {
        if (!multiplayerSessionQuestions.containsKey(code)) {
            return null;
        }
        return new ArrayList<>(multiplayerSessionQuestions.get(code));
    }
}
