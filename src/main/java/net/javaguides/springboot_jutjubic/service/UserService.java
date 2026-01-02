package net.javaguides.springboot_jutjubic.service;

import java.util.List;
import net.javaguides.springboot_jutjubic.dto.UserRequest;
import net.javaguides.springboot_jutjubic.model.User;

public interface UserService {
    User findById(Long id);
    User findByUsername(String username);
    User findByEmail(String email);
    List<User> findAll();
    User save(UserRequest userRequest);
    boolean activateUser(String token);
}