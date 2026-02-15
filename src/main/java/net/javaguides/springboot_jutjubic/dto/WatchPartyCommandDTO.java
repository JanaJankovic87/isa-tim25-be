package net.javaguides.springboot_jutjubic.dto;

public class WatchPartyCommandDTO {
    private String roomId;
    private Long videoId;
    private String userId;
    private String action;

    public WatchPartyCommandDTO() {}

 
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public Long getVideoId() { return videoId; }
    public void setVideoId(Long videoId) { this.videoId = videoId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}