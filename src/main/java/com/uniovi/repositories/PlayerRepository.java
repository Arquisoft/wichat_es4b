package com.uniovi.repositories;

import com.uniovi.entities.Player;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface PlayerRepository extends CrudRepository<Player, Long> {
    Player findByEmail(String email);
    Player findByUsername(String nickname);
    @Query("SELECT player FROM Player player WHERE player.multiplayerCode=:multiplayerCode")
    Iterable<Player> findAllByMultiplayerCode(int multiplayerCode);

    Page<Player> findAll(Pageable pageable);
}
