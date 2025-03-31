package com.uniovi.services.impl;

import com.uniovi.dto.RoleImageDto;
import com.uniovi.entities.RoleImage;
import com.uniovi.repositories.RoleImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoleServiceImageImpl {
    private final RoleImageRepository roleRepository;

    @Autowired
    public RoleServiceImageImpl(RoleImageRepository roleRepository) {
        this.roleRepository = roleRepository;
    }


    public RoleImage addRole(RoleImageDto role) {
        RoleImage foundRole = roleRepository.findById(role.getName()).orElse(null);
        if (foundRole != null) {
           return foundRole;
        }

        RoleImage newRole = new RoleImage(role.getName());
        roleRepository.save(newRole);
        return newRole;
    }


    public RoleImage getRole(String name) {
        return roleRepository.findById(name).orElse(null);
    }


    public List<RoleImage> getAllRoles() {
        List<RoleImage> roles = new ArrayList<>();
        roleRepository.findAll().forEach(roles::add);
        return roles;
    }
}
