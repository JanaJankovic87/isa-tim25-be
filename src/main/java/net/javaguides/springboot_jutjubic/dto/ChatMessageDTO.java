package net.javaguides.springboot_jutjubic.dto;

import java.time.LocalDateTime;

public class ChatMessageDTO {
    private String username;
    private String message;
    private Long videoId;
    private LocalDateTime timestamp;

    public ChatMessageDTO() {
        this.timestamp = LocalDateTime.now();
    }


    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getVideoId() { return videoId; }
    public void setVideoId(Long videoId) { this.videoId = videoId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}