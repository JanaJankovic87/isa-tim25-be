package net.javaguides.springboot_jutjubic.dto;

public class VideoPopularityDTO implements Comparable<VideoPopularityDTO>{
    private Long videoId;
    private Double popularityScore;
    private Long totalViews;

    public VideoPopularityDTO() {
    }

    public VideoPopularityDTO(Long videoId, Double popularityScore, Long totalViews) {
        this.videoId = videoId;
        this.popularityScore = popularityScore;
        this.totalViews = totalViews;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
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

    @Override
    public int compareTo(VideoPopularityDTO other) {
        // sort za popularnost, od najpopularnijeg do najmanje popularnog
        return Double.compare(other.popularityScore, this.popularityScore);
    }

    @Override
    public String toString() {
        return "VideoPopularityDTO{" +
                "videoId=" + videoId +
                ", popularityScore=" + popularityScore +
                ", totalViews=" + totalViews +
                '}';
    }
}
