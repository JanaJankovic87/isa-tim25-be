package net.javaguides.springboot_jutjubic.dto;

import java.time.LocalDateTime;
import java.util.List;

public class VideoDTO {

    private Long id;
    private String title;
    private String description;
    private List<String> tags;
    private String thumbnailPath;
    private String videoPath;
    private LocalDateTime createdAt;

    // Geolocation fields (NOVO za S2)
    private String location;
    private Double latitude;
    private Double longitude;
    private Boolean isLocationApproximated;

    private Long userId;
    private String username; // Ako treba prikazati ko je postavio

    // Statistics (opciono)
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;

    private Integer version;

    // Constructors
    public VideoDTO() {
    }

    public VideoDTO(Long id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    // Full constructor
    public VideoDTO(Long id, String title, String description, List<String> tags,
                    String thumbnailPath, String videoPath, LocalDateTime createdAt,
                    String location, Double latitude, Double longitude,
                    Boolean isLocationApproximated, Long userId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.thumbnailPath = thumbnailPath;
        this.videoPath = videoPath;
        this.createdAt = createdAt;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isLocationApproximated = isLocationApproximated;
        this.userId = userId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Boolean getIsLocationApproximated() {
        return isLocationApproximated;
    }

    public void setIsLocationApproximated(Boolean isLocationApproximated) {
        this.isLocationApproximated = isLocationApproximated;
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

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "VideoDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", tags=" + tags +
                ", location='" + location + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", isLocationApproximated=" + isLocationApproximated +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", viewCount=" + viewCount +
                ", likeCount=" + likeCount +
                ", commentCount=" + commentCount +
                ", createdAt=" + createdAt +
                '}';
    }
}