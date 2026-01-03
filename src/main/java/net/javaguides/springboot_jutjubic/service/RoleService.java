package net.javaguides.springboot_jutjubic.service;

import java.util.List;
import net.javaguides.springboot_jutjubic.model.Role;

public interface RoleService {
    Role findById(Long id);
    List<Role> findByName(String name);
}
