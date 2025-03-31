package com.uniovi.services;

import com.uniovi.entities.Player;
import com.uniovi.entities.abstracts.AbstractGameSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GameSessionService<T extends AbstractGameSession<?>> {
    Integer NORMAL_GAME_QUESTION_NUM = 100;

    /**
     * Return the list of GameSessions
     *
     * @return the list of GameSessions
     */
    List<T> getGameSessions();

    /**
     * Return the list of GameSessions by player
     *
     * @return the list of GameSessions by player
     */
    List<T> getGameSessionsByPlayer(Player player);

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
    Page<T> getPlayerRanking(Pageable pageable, Player player);

    T startNewGame(Player player);

    T startNewMultiplayerGame(Player player, int code);

    void endGame(T gameSession);
}
