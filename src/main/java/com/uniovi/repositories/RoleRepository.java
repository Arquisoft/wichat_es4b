package com.uniovi.repositories;

import com.uniovi.entities.Role;
import org.checkerframework.common.aliasing.qual.Unique;
import org.springframework.data.repository.CrudRepository;

public interface RoleRepository extends CrudRepository<Role, String> {
	boolean getRoleByName(@Unique String name);
}
