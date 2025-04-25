package com.uniovi.services.impl;

import com.uniovi.entities.ApiKey;
import com.uniovi.entities.Associations;
import com.uniovi.entities.Player;
import com.uniovi.repositories.ApiKeyRepository;
import com.uniovi.services.ApiKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApiKeyServiceImpl implements ApiKeyService {
    private final ApiKeyRepository apiKeyRepository;

    @Autowired
    public ApiKeyServiceImpl(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    public void createApiKey(Player forPlayer) {
        ApiKey apiKey = new ApiKey();
        Associations.PlayerApiKey.addApiKey(forPlayer, apiKey);
        apiKeyRepository.save(apiKey);
	}

    @Override
    public ApiKey getApiKey(String apiKey) {
        return apiKeyRepository.findByKeyToken(apiKey);
    }
}
