package com.uniovi.services;

import com.uniovi.entities.GameSession;
import com.uniovi.entities.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GameSessionService {
    Integer NORMAL_GAME_QUESTION_NUM = 4;

    /**
     * Return the list of GameSessions
     *
     * @return the list of GameSessions
     */
    List<GameSession> getGameSessions();

    /**
     * Return the list of GameSessions by player
     *
     * @return the list of GameSessions by player
     */
    List<GameSession> getGameSessionsByPlayer(Player player);

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
    Page<GameSession> getPlayerRanking(Pageable pageable, Player player);

    GameSession startNewGame(Player player);

    GameSession startNewMultiplayerGame(Player player, int code);

    void endGame(GameSession gameSession);
}
