package com.uniovi.repositories;

import com.uniovi.entities.GameSession;
import com.uniovi.entities.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GameSessionRepository extends CrudRepository<GameSession, Long> {

    List<GameSession> findAll();

    List<GameSession> findAllByPlayer(Player player);

    @Query("SELECT gs.player, SUM(gs.score) FROM GameSession gs GROUP BY gs.player ORDER BY SUM(gs.score) DESC")
    Page<Object[]> findTotalScoresByPlayer(Pageable pageable);
    Page<GameSession> findAllByPlayerOrderByScoreDesc(Pageable pageable, Player player);
}
