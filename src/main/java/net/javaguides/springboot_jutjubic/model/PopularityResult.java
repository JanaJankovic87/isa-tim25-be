package net.javaguides.springboot_jutjubic.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "POPULARITY_RESULTS")
public class PopularityResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pipeline_run_at", nullable = false)
    private LocalDateTime pipelineRunAt;

    @Column(name = "video_id", nullable = false)
    private Long videoId;

    @Column(name = "popularity_score", nullable = false)
    private Double popularityScore;

    @Column(name = "rank_position", nullable = false)
    private Integer rankPosition;

    @Column(name = "view_count")
    private Long viewCount;

    // Constructors
    public PopularityResult() {
        this.pipelineRunAt = LocalDateTime.now();
    }

    public PopularityResult(Long videoId, Double popularityScore, Integer rankPosition, Long viewCount) {
        this.videoId = videoId;
        this.popularityScore = popularityScore;
        this.rankPosition = rankPosition;
        this.viewCount = viewCount;
        this.pipelineRunAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getPipelineRunAt() {
        return pipelineRunAt;
    }

    public void setPipelineRunAt(LocalDateTime pipelineRunAt) {
        this.pipelineRunAt = pipelineRunAt;
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

    public Integer getRankPosition() {
        return rankPosition;
    }

    public void setRankPosition(Integer rankPosition) {
        this.rankPosition = rankPosition;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    @Override
    public String toString() {
        return "PopularityResult{" +
                "id=" + id +
                ", videoId=" + videoId +
                ", popularityScore=" + popularityScore +
                ", rankPosition=" + rankPosition +
                ", viewCount=" + viewCount +
                ", pipelineRunAt=" + pipelineRunAt +
                '}';
    }
}
