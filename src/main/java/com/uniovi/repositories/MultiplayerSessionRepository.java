package com.uniovi.repositories;

import com.uniovi.entities.GameSession;
import com.uniovi.entities.MultiplayerSession;
import com.uniovi.entities.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MultiplayerSessionRepository extends CrudRepository<MultiplayerSession, Long> {
    MultiplayerSession findByMultiplayerCode(String code);
}

