package net.javaguides.springboot_jutjubic.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import net.javaguides.springboot_jutjubic.dto.UserRequest;
import net.javaguides.springboot_jutjubic.model.Role;
import net.javaguides.springboot_jutjubic.model.User;
import net.javaguides.springboot_jutjubic.repository.UserRepository;
import net.javaguides.springboot_jutjubic.service.RoleService;
import net.javaguides.springboot_jutjubic.service.UserService;
import net.javaguides.springboot_jutjubic.service.EmailService;
import net.javaguides.springboot_jutjubic.model.VerificationToken;
import net.javaguides.springboot_jutjubic.repository.VerificationTokenRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleService roleService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

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
        user.setEnabled(false);
        user.setLastPasswordResetDate(new Timestamp(System.currentTimeMillis()));

        List<Role> roles = roleService.findByName("ROLE_USER");
        user.setRoles(roles);

        user = userRepository.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, user);
        verificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user.getEmail(), token, user.getUsername());

        return user;
    }

    @Transactional
    public boolean activateUser(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElse(null);

        if (verificationToken == null) {
            return false;
        }

        if (verificationToken.isExpired()) {
            return false;
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);

        return true;
    }
}
