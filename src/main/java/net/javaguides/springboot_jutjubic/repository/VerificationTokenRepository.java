package net.javaguides.springboot_jutjubic.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import net.javaguides.springboot_jutjubic.model.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long>
{
    Optional<VerificationToken> findByToken(String token);
}
