package net.javaguides.springboot_jutjubic.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;





@Entity
@Table(name = "VIDEO_POSTS")
@NamedQuery(name = "VideoPost.findByUserId",
        query = "select v from Video v where v.userId=?1")
public class Video implements Serializable {

    public enum TranscodingStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @ElementCollection
    @CollectionTable(name = "video_post_tags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "tag")
    private List<String> tags;

    @Column(name = "thumbnail_path")
    private String thumbnailPath;

    @Column(name = "video_path")
    private String videoPath;

    @Column(name = "original_video_path")
    private String originalVideoPath;

    @Column(name = "transcoded_dir")
    private String transcodedDir;

    @Column(name = "transcoding_status")
    @Enumerated(EnumType.STRING)
    private TranscodingStatus transcodingStatus = TranscodingStatus.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "location")
    private String location;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "is_location_approximated")
    private Boolean isLocationApproximated = false;

    @Column(name = "user_id")
    private Long userId;

    @Version
    private Integer version;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Column(name = "is_scheduled")
    private Boolean isScheduled = false;

    @Column(name = "video_duration_seconds")
    private Long videoDurationSeconds; // trajanje videa u sekundama

    public enum VideoStatus {
        REGULAR,      // Običan video (nije scheduled)
        SCHEDULED,    // Zakazan, ali još nije počeo
        LIVE,         // Trenutno se emituje (stream je aktivan)
        ENDED         // Stream je završen
    }

    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    public Video() {
        super();
        this.createdAt = LocalDateTime.now();
    }

    public Video(String title, String description, List<String> tags, Long userId) {
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public String getOriginalVideoPath() {
        return originalVideoPath;
    }

    public void setOriginalVideoPath(String originalVideoPath) {
        this.originalVideoPath = originalVideoPath;
    }

    public String getTranscodedDir() {
        return transcodedDir;
    }

    public void setTranscodedDir(String transcodedDir) {
        this.transcodedDir = transcodedDir;
    }


    public TranscodingStatus getTranscodingStatus() {
        return transcodingStatus;
    }
    public void setTranscodingStatus(TranscodingStatus transcodingStatus) {
        this.transcodingStatus = transcodingStatus;
    }

    @Override
    public String toString() {
        return "Video [id=" + id + ", title=" + title + ", userId=" + userId +
                ", createdAt=" + createdAt + ", latitude=" + latitude +
                ", longitude=" + longitude + ", version=" + version + "]";
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public Boolean getIsScheduled() {
        return isScheduled;
    }

    public void setIsScheduled(Boolean isScheduled) {
        this.isScheduled = isScheduled;
    }

    public Long getVideoDurationSeconds() {
        return videoDurationSeconds;
    }

    public void setVideoDurationSeconds(Long videoDurationSeconds) {
        this.videoDurationSeconds = videoDurationSeconds;
    }
}