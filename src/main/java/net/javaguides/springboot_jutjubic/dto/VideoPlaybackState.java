package net.javaguides.springboot_jutjubic.dto;

import java.time.LocalDateTime;

public class VideoPlaybackState {

    private Long videoId;
    private LocalDateTime scheduledTime;
    private Long videoDurationSeconds;
    private Integer currentSecond;
    private Boolean isLive;
    private Boolean hasEnded;

    public VideoPlaybackState() {}

    public VideoPlaybackState(Long videoId, LocalDateTime scheduledTime,
                              Long videoDurationSeconds, Integer currentSecond) {
        this.videoId = videoId;
        this.scheduledTime = scheduledTime;
        this.videoDurationSeconds = videoDurationSeconds;
        this.currentSecond = currentSecond;
        this.isLive = currentSecond >= 0;
        this.hasEnded = currentSecond >= videoDurationSeconds;
    }

    // Getteri i setteri
    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public Long getVideoDurationSeconds() {
        return videoDurationSeconds;
    }

    public void setVideoDurationSeconds(Long videoDurationSeconds) {
        this.videoDurationSeconds = videoDurationSeconds;
    }

    public Integer getCurrentSecond() {
        return currentSecond;
    }

    public void setCurrentSecond(Integer currentSecond) {
        this.currentSecond = currentSecond;
    }

    public Boolean getIsLive() {
        return isLive;
    }

    public void setIsLive(Boolean isLive) {
        this.isLive = isLive;
    }

    public Boolean getHasEnded() {
        return hasEnded;
    }

    public void setHasEnded(Boolean hasEnded) {
        this.hasEnded = hasEnded;
    }
}