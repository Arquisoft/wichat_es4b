package com.uniovi.services.impl;

import com.uniovi.entities.MultiplayerSession;
import com.uniovi.entities.Player;
import com.uniovi.entities.QuestionImage;
import com.uniovi.repositories.MultiplayerSessionRepository;
import com.uniovi.repositories.PlayerRepository;
import com.uniovi.services.GameSessionService;
import com.uniovi.services.MultiplayerSessionService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MultiplayerSessionImageServiceImpl implements MultiplayerSessionService<QuestionImage> {

    private final PlayerRepository playerRepository;
    private final MultiplayerSessionRepository multiplayerSessionRepository;
    private final QuestionImageServiceImpl questionService;

    private final Map<String, List<QuestionImage>> multiplayerSessionQuestions = new HashMap<>();


    public MultiplayerSessionImageServiceImpl(PlayerRepository playerRepository, MultiplayerSessionRepository multiplayerSessionRepository,
                                              QuestionImageServiceImpl questionService) {
        this.playerRepository = playerRepository;
        this.multiplayerSessionRepository = multiplayerSessionRepository;
        this.questionService = questionService;
    }

    @Override
    @Transactional
    public Map<Player, Integer> getPlayersWithScores(int multiplayerCode) {
        MultiplayerSession session = multiplayerSessionRepository.findByMultiplayerCode(String.valueOf(multiplayerCode));
        Map<Player, Integer> playerScores = session.getPlayerScores();

        // Ordenar los jugadores por puntuación de mayor a menor
        List<Player> sortedPlayers = playerScores.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).map(Map.Entry::getKey).toList();

        Map<Player, Integer> playersSorted = new HashMap<>();
        for (Player player : sortedPlayers) {
            playersSorted.put(player, playerScores.get(player));
        }
        return playersSorted;
    }

    @Override
    public void multiCreate(String code, Long id) {
        Player p = playerRepository.findById(id).orElse(null);

        if (p != null) {
            multiplayerSessionRepository.save(new MultiplayerSession(code, p));
            multiplayerSessionQuestions.put(code, questionService.getRandomQuestions(GameSessionService.NORMAL_GAME_QUESTION_NUM));
        }
    }

    @Override
    @Transactional
    public void addToLobby(String code, Long id) {
        Player p = playerRepository.findById(id).orElse(null);

        if (p != null) {
            MultiplayerSession ms = multiplayerSessionRepository.findByMultiplayerCode(code);
            ms.addPlayer(p);
            multiplayerSessionRepository.save(ms);
        }
    }

    @Override
    @Transactional
    public void changeScore(String code, Long id, int score) {
        Player p = playerRepository.findById(id).orElse(null);

        if (p != null) {
            MultiplayerSession ms = multiplayerSessionRepository.findByMultiplayerCode(code);
            ms.getPlayerScores().put(p, score);
            multiplayerSessionRepository.save(ms);
        }
    }

    @Override
    public boolean existsCode(String code) {
        return multiplayerSessionRepository.findByMultiplayerCode(code) != null;
    }


    @Override
    public List<QuestionImage> getQuestions(String code) {
        if (!multiplayerSessionQuestions.containsKey(code)) {
            return null;
        }
        return new ArrayList<>(multiplayerSessionQuestions.get(code));
    }
}
