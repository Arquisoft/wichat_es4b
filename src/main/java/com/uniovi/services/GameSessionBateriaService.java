package com.uniovi.services;

import com.uniovi.entities.GameSessionBateria;
import com.uniovi.entities.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GameSessionBateriaService {
    /**
     * Return the list of GameSessions
     *
     * @return the list of GameSessions
     */
    List<GameSessionBateria> getGameSessions();

    /**
     * Return the list of GameSessions by player
     *
     * @return the list of GameSessions by player
     */
    List<GameSessionBateria> getGameSessionsByPlayer(Player player);

     /* Return the global ranking
     *
     * @param pageable the pageable
     * @return the global ranking
     */
    Page<Object[]> getGlobalRanking(Pageable pageable);

    /**
     * Return the player ranking
     *
     * @param pageable the pageable
     * @param player the player
     * @return the player ranking
     */
    Page<GameSessionBateria> getPlayerRanking(Pageable pageable, Player player);

    GameSessionBateria startNewGame(Player player);

    GameSessionBateria startNewMultiplayerGame(Player player, int code);

    void endGame(GameSessionBateria gameSession);
}
