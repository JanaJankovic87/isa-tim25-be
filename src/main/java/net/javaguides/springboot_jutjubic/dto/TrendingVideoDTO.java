package net.javaguides.springboot_jutjubic.dto;

import net.javaguides.springboot_jutjubic.model.Video;

public class TrendingVideoDTO {
    private Long videoId;
    private String title;
    private String thumbnailPath;
    private Double popularityScore;  // S1 će računati
    private Long viewCount;
    private Double distanceKm;       // NOVO - za lokalni trending
    private String location;         // NOVO - gde je video snimljen

    // Constructor za GLOBALNI trending (postojeći)
    public TrendingVideoDTO(Video video, double trendingScore) {
        this.videoId = video.getId();
        this.title = video.getTitle();
        this.thumbnailPath = video.getThumbnailPath();
        this.popularityScore = trendingScore;
        this.viewCount = 0L; // Popuniće se kasnije
        this.distanceKm = null; // Nema distance za globalni
        this.location = video.getLocation();
    }

    // Constructor za LOKALNI trending (S2 novi)
    public TrendingVideoDTO(Video video, double popularityScore, double distanceKm) {
        this.videoId = video.getId();
        this.title = video.getTitle();
        this.thumbnailPath = video.getThumbnailPath();
        this.popularityScore = popularityScore;
        this.distanceKm = distanceKm;
        this.location = video.getLocation();
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

    // Backward compatibility
    public Video getVideo() {
        Video v = new Video();
        v.setId(videoId);
        v.setTitle(title);
        v.setThumbnailPath(thumbnailPath);
        v.setLocation(location);
        return v;
    }

    public double getTrendingScore() {
        return popularityScore != null ? popularityScore : 0.0;
    }
}