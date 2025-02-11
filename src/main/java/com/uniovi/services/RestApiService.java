package com.uniovi.services;

import com.uniovi.entities.ApiKey;
import com.uniovi.entities.Player;
import com.uniovi.entities.Question;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;

public interface RestApiService {
    /**
     * Get all the players in the database
     * @param params A map with the parameters of the request
     * @return A list with all the players
     */
    List<Player> getPlayers(Map<String, String> params);

    /**
     * Log an access to the API
     * @param apiKey The API key used to access the API
     * @param path The path of the request
     * @param params The parameters of the request
     */
    void logAccess(ApiKey apiKey, String path, Map<String, String> params);

    /**
     * Get all the questions in the database
     * @param params A map with the parameters of the request
     * @return A list with all the questions
     */
    List<Question> getQuestions(Map<String, String> params, Pageable pageable);
}
