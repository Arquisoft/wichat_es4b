package com.uniovi.services;

import com.uniovi.entities.GameSession;
import com.uniovi.entities.MultiplayerSession;
import com.uniovi.entities.Player;
import com.uniovi.entities.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface MultiplayerSessionService {

    Map<Player, Integer> getPlayersWithScores(int multiplayerCode);
    void multiCreate(String code, Long id);

    void addToLobby(String code, Long id);

    void changeScore(String code,Long id,int score);

    boolean existsCode(String code);

    List<Question> getQuestions(String code);
}
