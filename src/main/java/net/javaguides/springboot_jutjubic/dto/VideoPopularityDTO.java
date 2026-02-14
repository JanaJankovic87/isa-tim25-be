package net.javaguides.springboot_jutjubic.dto;

public class VideoPopularityDTO implements Comparable<VideoPopularityDTO>{
    private Long videoId;
    private String title;
    private String thumbnailPath;
    private Double popularityScore;
    private Long totalViews;
    private Long likesCount;
    private String location;
    private Double latitude;
    private Double longitude;

    public VideoPopularityDTO() {
    }

    public VideoPopularityDTO(Long videoId, Double popularityScore, Long totalViews) {
        this.videoId = videoId;
        this.popularityScore = popularityScore;
        this.totalViews = totalViews;
    }

    // Konstruktor sa svim poljima za prikaz
    public VideoPopularityDTO(Long videoId, String title, String thumbnailPath,
                             Double popularityScore, Long totalViews, Long likesCount,
                             String location, Double latitude, Double longitude) {
        this.videoId = videoId;
        this.title = title;
        this.thumbnailPath = thumbnailPath;
        this.popularityScore = popularityScore;
        this.totalViews = totalViews;
        this.likesCount = likesCount;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public Double getPopularityScore() {
        return popularityScore;
    }

    public void setPopularityScore(Double popularityScore) {
        this.popularityScore = popularityScore;
    }

    public Long getTotalViews() {
        return totalViews;
    }

    public void setTotalViews(Long totalViews) {
        this.totalViews = totalViews;
    }

    public Long getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(Long likesCount) {
        this.likesCount = likesCount;
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

    @Override
    public int compareTo(VideoPopularityDTO other) {
        // sort za popularnost, od najpopularnijeg do najmanje popularnog
        return Double.compare(other.popularityScore, this.popularityScore);
    }

    @Override
    public String toString() {
        return "VideoPopularityDTO{" +
                "videoId=" + videoId +
                ", title='" + title + '\'' +
                ", thumbnailPath='" + thumbnailPath + '\'' +
                ", popularityScore=" + popularityScore +
                ", totalViews=" + totalViews +
                ", likesCount=" + likesCount +
                ", location='" + location + '\'' +
                '}';
    }
}
