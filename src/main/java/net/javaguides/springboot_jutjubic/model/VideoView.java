package net.javaguides.springboot_jutjubic.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "VIDEO_VIEWS",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "video_id"}))
public class VideoView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "video_id", nullable = false)
    private Long videoId;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "location_name")
    private String locationName;

    @Column(name = "is_location_approximated")
    private Boolean isLocationApproximated = false;

    // Constructors

    public VideoView() {
        this.viewedAt = LocalDateTime.now();
    }

    public VideoView(Long userId, Long videoId) {
        this.userId = userId;
        this.videoId = videoId;
        this.viewedAt = LocalDateTime.now();
    }

    public VideoView(Long userId, Long videoId, Double latitude, Double longitude) {
        this.userId = userId;
        this.videoId = videoId;
        this.viewedAt = LocalDateTime.now();
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and Setters

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

    public LocalDateTime getViewedAt() {
        return viewedAt;
    }

    public void setViewedAt(LocalDateTime viewedAt) {
        this.viewedAt = viewedAt;
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