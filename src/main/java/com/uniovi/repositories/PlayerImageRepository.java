package com.uniovi.repositories;

import com.uniovi.entities.PlayerImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface PlayerImageRepository extends CrudRepository<PlayerImage, Long> {
    PlayerImage findByEmail(String email);
    PlayerImage findByUsername(String nickname);
    @Query("SELECT player FROM PlayerImage player WHERE player.multiplayerCode=:multiplayerCode")
    Iterable<PlayerImage> findAllByMultiplayerCode(int multiplayerCode);

    Page<PlayerImage> findAll(Pageable pageable);
}
