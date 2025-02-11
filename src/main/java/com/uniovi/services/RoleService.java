package com.uniovi.services;

import com.uniovi.dto.RoleDto;
import com.uniovi.entities.Role;

import java.util.List;

public interface RoleService {
    /**
     * Add a new role to the database
     * @param role RoleDto with the information of the new role
     * @return The new role
     * @throws IllegalArgumentException if the role already exists
     */
    Role addRole(RoleDto role);

    /**
     * Get a role by its name
     * @param name The name of the role
     * @return The role with the given name
     */
    Role getRole(String name);

    /**
     * Get all the roles in the database
     * @return A list with all the roles
     */
    List<Role> getAllRoles();
}
