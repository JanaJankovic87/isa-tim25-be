package net.javaguides.springboot_jutjubic.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import net.javaguides.springboot_jutjubic.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByEmail(String email);
}
