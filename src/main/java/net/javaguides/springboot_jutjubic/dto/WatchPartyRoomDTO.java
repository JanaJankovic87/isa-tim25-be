package net.javaguides.springboot_jutjubic.dto;

public class WatchPartyRoomDTO {
    private String roomId;
    private Long ownerId;
    private String ownerUsername;
    private Long currentVideoId;
    private boolean active;

    public WatchPartyRoomDTO() {}

    public WatchPartyRoomDTO(String roomId, Long ownerId, String ownerUsername,
                             Long currentVideoId, boolean active) {
        this.roomId = roomId;
        this.ownerId = ownerId;
        this.ownerUsername = ownerUsername;
        this.currentVideoId = currentVideoId;
        this.active = active;
    }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }

    public Long getCurrentVideoId() { return currentVideoId; }
    public void setCurrentVideoId(Long currentVideoId) { this.currentVideoId = currentVideoId; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}