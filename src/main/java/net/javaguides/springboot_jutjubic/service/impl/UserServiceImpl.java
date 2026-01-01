package net.javaguides.springboot_jutjubic.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import net.javaguides.springboot_jutjubic.dto.UserRequest;
import net.javaguides.springboot_jutjubic.model.Role;
import net.javaguides.springboot_jutjubic.model.User;
import net.javaguides.springboot_jutjubic.repository.UserRepository;
import net.javaguides.springboot_jutjubic.service.RoleService;
import net.javaguides.springboot_jutjubic.service.UserService;

import java.sql.Timestamp;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleService roleService;

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User save(UserRequest userRequest) {
        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setEmail(userRequest.getEmail());
        user.setFirstName(userRequest.getFirstname());
        user.setLastName(userRequest.getLastname());
        user.setAddress(userRequest.getAddress());

        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        user.setEnabled(true);

        user.setLastPasswordResetDate(new Timestamp(System.currentTimeMillis()));

        List<Role> roles = roleService.findByName("ROLE_USER");
        user.setRoles(roles);

        return userRepository.save(user);
    }
}
