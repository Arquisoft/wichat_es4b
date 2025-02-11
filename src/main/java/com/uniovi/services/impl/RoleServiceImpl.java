package com.uniovi.services.impl;

import com.uniovi.dto.RoleDto;
import com.uniovi.entities.Role;
import com.uniovi.repositories.RoleRepository;
import com.uniovi.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Role addRole(RoleDto role) {
        Role foundRole = roleRepository.findById(role.getName()).orElse(null);
        if (foundRole != null) {
           return foundRole;
        }

        Role newRole = new Role(role.getName());
        roleRepository.save(newRole);
        return newRole;
    }

    @Override
    public Role getRole(String name) {
        return roleRepository.findById(name).orElse(null);
    }

    @Override
    public List<Role> getAllRoles() {
        List<Role> roles = new ArrayList<>();
        roleRepository.findAll().forEach(roles::add);
        return roles;
    }
}
