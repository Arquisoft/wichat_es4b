package com.uniovi.services;

import com.uniovi.entities.ApiKey;
import com.uniovi.entities.Player;

public interface ApiKeyService {
    ApiKey createApiKey(Player forPlayer);
    ApiKey getApiKey(String apiKey);
}
