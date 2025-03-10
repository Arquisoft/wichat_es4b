package com.uniovi.repositories;

import com.uniovi.entities.Role;
import com.uniovi.entities.RoleImage;
import org.springframework.data.repository.CrudRepository;

public interface RoleImageRepository extends CrudRepository<RoleImage, String> {
}
