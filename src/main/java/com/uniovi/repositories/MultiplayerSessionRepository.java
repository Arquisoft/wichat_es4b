package com.uniovi.repositories;

import com.uniovi.entities.MultiplayerSession;
import org.springframework.data.repository.CrudRepository;

public interface MultiplayerSessionRepository extends CrudRepository<MultiplayerSession, Long> {

    MultiplayerSession findByMultiplayerCode(String code);
}

