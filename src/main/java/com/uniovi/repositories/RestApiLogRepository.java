package com.uniovi.repositories;

import com.uniovi.entities.ApiKey;
import com.uniovi.entities.Player;
import com.uniovi.entities.RestApiAccessLog;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface RestApiLogRepository extends CrudRepository<RestApiAccessLog, Long> {
    List<RestApiAccessLog> findByApiKey(ApiKey apiKey);

    @Query("SELECT r FROM RestApiAccessLog r WHERE r.apiKey.player = ?1")
    List<RestApiAccessLog> findByUser(Player user);
}
