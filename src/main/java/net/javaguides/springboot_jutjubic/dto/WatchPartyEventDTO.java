package net.javaguides.springboot_jutjubic.dto;

public class WatchPartyEventDTO {

    public enum EventType {
        REDIRECT_VIDEO,
        ROOM_CLOSED,
        USER_JOINED,
        USER_LEFT
    }

    private EventType eventType;
    private String roomId;
    private Long videoId;
    private String username;
    private String message;

    public WatchPartyEventDTO() {}

    public static WatchPartyEventDTO redirectVideo(String roomId, Long videoId, String username) {
        WatchPartyEventDTO e = new WatchPartyEventDTO();
        e.eventType = EventType.REDIRECT_VIDEO;
        e.roomId = roomId;
        e.videoId = videoId;
        e.username = username;
        e.message = "Owner started video " + videoId;
        return e;
    }

    public static WatchPartyEventDTO roomClosed(String roomId, String username) {
        WatchPartyEventDTO e = new WatchPartyEventDTO();
        e.eventType = EventType.ROOM_CLOSED;
        e.roomId = roomId;
        e.username = username;
        e.message = "Room was closed by owner";
        return e;
    }

    public static WatchPartyEventDTO userJoined(String roomId, String username) {
        WatchPartyEventDTO e = new WatchPartyEventDTO();
        e.eventType = EventType.USER_JOINED;
        e.roomId = roomId;
        e.username = username;
        e.message = username + " joined the room";
        return e;
    }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public Long getVideoId() { return videoId; }
    public void setVideoId(Long videoId) { this.videoId = videoId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}