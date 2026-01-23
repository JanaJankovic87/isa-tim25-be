package net.javaguides.springboot_jutjubic.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "video_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "video_id"})
})
public class VideoLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "video_id", nullable = false)
    private Long videoId;

    @Column(name = "liked_at", nullable = false)
    private LocalDateTime likedAt;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "location_name")
    private String locationName;

    @Column(name = "is_location_approximated")
    private Boolean isLocationApproximated = false;

    public VideoLike() {
        this.likedAt = LocalDateTime.now();
    }

    public VideoLike(Long userId, Long videoId) {
        this.userId = userId;
        this.videoId = videoId;
        this.likedAt = LocalDateTime.now();
    }

    public VideoLike(Long userId, Long videoId,  Double latitude, Double longitude) {
        this.userId = userId;
        this.videoId = videoId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.likedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public LocalDateTime getLikedAt() {
        return likedAt;
    }

    public void setLikedAt(LocalDateTime likedAt) {
        this.likedAt = likedAt;
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

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Boolean getIsLocationApproximated() {
        return isLocationApproximated;
    }

    public void setIsLocationApproximated(Boolean isLocationApproximated) {
        this.isLocationApproximated = isLocationApproximated;
    }
}

