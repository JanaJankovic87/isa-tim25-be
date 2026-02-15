package net.javaguides.springboot_jutjubic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WatchPartyEventDTO {

    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("eventType")
    private String eventType;

    @JsonProperty("videoId")
    private Long videoId;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("username")
    private String username;

    @JsonProperty("message")
    private String message;

    public static final String ROOM_CREATED = "ROOM_CREATED";
    public static final String USER_JOINED = "USER_JOINED";
    public static final String USER_LEFT = "USER_LEFT";
    public static final String REDIRECT_VIDEO = "REDIRECT_VIDEO";
    public static final String ROOM_CLOSED = "ROOM_CLOSED";

    public WatchPartyEventDTO() {}

    public WatchPartyEventDTO(String roomId, String eventType, Long videoId, String userId, String username, String message) {
        this.roomId = roomId;
        this.eventType = eventType;
        this.videoId = videoId;
        this.userId = userId;
        this.username = username;
        this.message = message;
    }

    public static WatchPartyEventDTO redirectVideo(String roomId, Long videoId, String username) {
        return new WatchPartyEventDTO(roomId, REDIRECT_VIDEO, videoId, null, username,
                "Video " + videoId + " is starting");
    }

    public static WatchPartyEventDTO userJoined(String roomId, String username) {
        return new WatchPartyEventDTO(roomId, USER_JOINED, null, null, username,
                username + " joined the room");
    }

    public static WatchPartyEventDTO userLeft(String roomId, String username) {
        return new WatchPartyEventDTO(roomId, USER_LEFT, null, null, username,
                username + " left the room");
    }

    public static WatchPartyEventDTO roomClosed(String roomId, String username) {
        return new WatchPartyEventDTO(roomId, ROOM_CLOSED, null, null, username,
                "Room closed by " + username);
    }

    // Getters and setters
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public Long getVideoId() { return videoId; }
    public void setVideoId(Long videoId) { this.videoId = videoId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    @Override
    public String toString() {
        return "WatchPartyEventDTO{" +
                "roomId='" + roomId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", videoId=" + videoId +
                ", username='" + username + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}