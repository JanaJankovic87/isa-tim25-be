package net.javaguides.springboot_jutjubic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class CommentDTO {

    private Long id;

    @NotBlank(message = "Comment text is required")
    @Size(min = 1, max = 2000, message = "Comment must be between 1 and 2000 characters")
    private String text;

    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;
    private Long videoId;

    public CommentDTO() {}

    public CommentDTO(Long id, String text, Long userId, String username,
                      String firstName, String lastName, LocalDateTime createdAt, Long videoId) {
        this.id = id;
        this.text = text;
        this.userId = userId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdAt = createdAt;
        this.videoId = videoId;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }
}