package com.uniovi.services.abstracts;

import com.uniovi.entities.MultiplayerSession;
import com.uniovi.entities.Player;
import com.uniovi.entities.abstracts.AbstractQuestion;
import com.uniovi.repositories.MultiplayerSessionRepository;
import com.uniovi.repositories.PlayerRepository;
import com.uniovi.services.MultiplayerSessionService;
import jakarta.transaction.Transactional;

import java.util.*;

public abstract class AbstractMultiplayerSessionService<T extends AbstractQuestion<?>> implements MultiplayerSessionService<T> {

    protected final PlayerRepository playerRepository;
    protected final MultiplayerSessionRepository multiplayerSessionRepository;
    protected final Map<String, List<T>> multiplayerSessionQuestions = new HashMap<>();

    protected AbstractMultiplayerSessionService(PlayerRepository playerRepository,
                                                MultiplayerSessionRepository multiplayerSessionRepository) {
        this.playerRepository = playerRepository;
        this.multiplayerSessionRepository = multiplayerSessionRepository;
    }

    protected abstract List<T> getRandomQuestions();

    @Override
    @Transactional
    public Map<Player, Integer> getPlayersWithScores(int multiplayerCode) {
        MultiplayerSession session = multiplayerSessionRepository.findByMultiplayerCode(String.valueOf(multiplayerCode));
        Map<Player, Integer> playerScores = session.getPlayerScores();

        List<Player> sortedPlayers = playerScores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .toList();

        Map<Player, Integer> playersSorted = new LinkedHashMap<>();
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
            multiplayerSessionQuestions.put(code, getRandomQuestions());
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
    public List<T> getQuestions(String code) {
        return multiplayerSessionQuestions.getOrDefault(code, Collections.emptyList());
    }
}
