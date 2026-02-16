package net.javaguides.springboot_jutjubic.repository;

import net.javaguides.springboot_jutjubic.model.WatchPartyRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchPartyRoomRepository extends JpaRepository<WatchPartyRoom, String> {
    Optional<WatchPartyRoom> findByRoomIdAndActiveTrue(String roomId);
    List<WatchPartyRoom> findByActiveTrue();
}