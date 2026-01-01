package net.javaguides.springboot_jutjubic.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import net.javaguides.springboot_jutjubic.model.Role;
import net.javaguides.springboot_jutjubic.repository.RoleRepository;
import net.javaguides.springboot_jutjubic.service.RoleService;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public Role findById(Long id) {
        return roleRepository.findById(id).orElse(null);
    }

    @Override
    public List<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }
}
