package net.javaguides.springboot_jutjubic.dto;

import net.javaguides.springboot_jutjubic.model.Video;

public class TrendingVideoDTO {
    private Long videoId;
    private String title;
    private String thumbnailPath;
    private Double popularityScore;
    private Long viewCount;
    private Double distanceKm;
    private String location;

    // DODAJ OVO:
    private Double latitude;
    private Double longitude;

    // Constructor za GLOBALNI trending (postojeći)
    public TrendingVideoDTO(Video video, double trendingScore) {
        this.videoId = video.getId();
        this.title = video.getTitle();
        this.thumbnailPath = video.getThumbnailPath();
        this.popularityScore = trendingScore;
        this.viewCount = 0L;
        this.distanceKm = null;
        this.location = video.getLocation();
        this.latitude = video.getLatitude();   // DODAJ
        this.longitude = video.getLongitude(); // DODAJ
    }

    // Constructor za LOKALNI trending (S2 novi)
    public TrendingVideoDTO(Video video, double popularityScore, double distanceKm) {
        this.videoId = video.getId();
        this.title = video.getTitle();
        this.thumbnailPath = video.getThumbnailPath();
        this.popularityScore = popularityScore;
        this.distanceKm = distanceKm;
        this.location = video.getLocation();
        this.latitude = video.getLatitude();   // DODAJ
        this.longitude = video.getLongitude(); // DODAJ
    }

    // Prazni constructor
    public TrendingVideoDTO() {}

    // Getters & Setters
    public Long getVideoId() { return videoId; }
    public void setVideoId(Long videoId) { this.videoId = videoId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getThumbnailPath() { return thumbnailPath; }
    public void setThumbnailPath(String thumbnailPath) { this.thumbnailPath = thumbnailPath; }

    public Double getPopularityScore() { return popularityScore; }
    public void setPopularityScore(Double popularityScore) { this.popularityScore = popularityScore; }

    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    // DODAJ OVO:
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    // Backward compatibility
    public Video getVideo() {
        Video v = new Video();
        v.setId(videoId);
        v.setTitle(title);
        v.setThumbnailPath(thumbnailPath);
        v.setLocation(location);
        v.setLatitude(latitude);   // DODAJ
        v.setLongitude(longitude); // DODAJ
        return v;
    }

    public double getTrendingScore() {
        return popularityScore != null ? popularityScore : 0.0;
    }
}