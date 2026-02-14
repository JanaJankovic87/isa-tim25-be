package net.javaguides.springboot_jutjubic.dto;

import java.time.LocalDateTime;

public class VideoViewByDayDTO {
    private Long videoId;
    private LocalDateTime viewDate;
    private Long viewCount;
    private Integer daysAgo;

    public VideoViewByDayDTO() {
    }

    public VideoViewByDayDTO(Long videoId, LocalDateTime viewDate, Long viewCount) {
        this.videoId = videoId;
        this.viewDate = viewDate;
        this.viewCount = viewCount;
    }

    // Getters and Setters
    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public LocalDateTime getViewDate() {
        return viewDate;
    }

    public void setViewDate(LocalDateTime viewDate) {
        this.viewDate = viewDate;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getDaysAgo() {
        return daysAgo;
    }

    public void setDaysAgo(Integer daysAgo) {
        this.daysAgo = daysAgo;
    }

    @Override
    public String toString() {
        return "VideoViewByDayDTO{" +
                "videoId=" + videoId +
                ", viewDate=" + viewDate +
                ", viewCount=" + viewCount +
                ", daysAgo=" + daysAgo +
                '}';
    }
}
