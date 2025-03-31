package com.uniovi.services;

import com.uniovi.entities.GameSession;
import com.uniovi.entities.Player;
import com.uniovi.entities.QuestionBase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GameSessionService<T extends QuestionBase> {
    Integer NORMAL_GAME_QUESTION_NUM = 100;

    /**
     * Return the list of GameSessions
     *
     * @return the list of GameSessions
     */
    List<GameSession<T>> getGameSessions();

    /**
     * Return the list of GameSessions by player
     *
     * @return the list of GameSessions by player
     */
    List<GameSession<T>> getGameSessionsByPlayer(Player player);

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
    Page<GameSession<T>> getPlayerRanking(Pageable pageable, Player player);

    GameSession<T> startNewGame(Player player);

    GameSession<T> startNewMultiplayerGame(Player player, int code);

    void endGame(GameSession<T> gameSession);
}
