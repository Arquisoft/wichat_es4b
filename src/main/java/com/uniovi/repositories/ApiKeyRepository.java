package com.uniovi.repositories;

import com.uniovi.entities.ApiKey;
import org.springframework.data.repository.CrudRepository;

public interface ApiKeyRepository extends CrudRepository<ApiKey, Long> {

    ApiKey findByKeyToken(String key);
}
