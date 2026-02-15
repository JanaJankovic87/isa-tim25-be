package net.javaguides.springboot_jutjubic.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "watch_party_rooms")
public class WatchPartyRoom {

    @Id
    @Column(name = "room_id", length = 36)
    private String roomId;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "owner_username", nullable = false)
    private String ownerUsername;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "current_video_id")
    private Long currentVideoId;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public WatchPartyRoom() {
        this.roomId = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    public WatchPartyRoom(Long ownerId, String ownerUsername) {
        this();
        this.ownerId = ownerId;
        this.ownerUsername = ownerUsername;
    }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getCurrentVideoId() { return currentVideoId; }
    public void setCurrentVideoId(Long currentVideoId) { this.currentVideoId = currentVideoId; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}