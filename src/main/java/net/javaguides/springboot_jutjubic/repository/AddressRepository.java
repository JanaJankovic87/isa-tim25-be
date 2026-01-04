package net.javaguides.springboot_jutjubic.repository;

import net.javaguides.springboot_jutjubic.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
}
