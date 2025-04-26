package com.uniovi.repositories;

import com.uniovi.entities.GameSessionImage;
import com.uniovi.entities.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GameSessionImageRepository extends CrudRepository<GameSessionImage, Long> {

    List<GameSessionImage> findAll();
    List<GameSessionImage> findAllByPlayer(Player player);

    @Query("SELECT gs.player, gs.score FROM GameSessionImage gs ORDER BY gs.score DESC")
    Page<Object[]> findScoresByPlayer(Pageable pageable);
    Page<GameSessionImage> findAllByPlayerOrderByScoreDesc(Pageable pageable, Player player);
}
