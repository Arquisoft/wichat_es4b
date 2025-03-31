package com.uniovi.repositories;

import com.uniovi.entities.MultiplayerSessionImage;
import org.springframework.data.repository.CrudRepository;

public interface MultiplayerSessionImageRepository extends CrudRepository<MultiplayerSessionImage, Long> {
    MultiplayerSessionImage findByMultiplayerCode(String code);
}

