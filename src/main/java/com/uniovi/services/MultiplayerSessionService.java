package com.uniovi.services;

import com.uniovi.entities.Player;
import com.uniovi.entities.abstracts.AbstractQuestion;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface MultiplayerSessionService<T extends AbstractQuestion<?>> {

    Map<Player, Integer> getPlayersWithScores(int multiplayerCode);
    void multiCreate(String code, Long id);

    void addToLobby(String code, Long id);

    void changeScore(String code,Long id,int score);

    boolean existsCode(String code);

    List<T> getQuestions(String code);
}
