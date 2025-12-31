package net.javaguides.springboot_jutjubic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import net.javaguides.springboot_jutjubic.model.Role;
import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findByName(String name);

}
