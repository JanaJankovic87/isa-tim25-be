package net.javaguides.springboot_jutjubic.dto;

import net.javaguides.springboot_jutjubic.model.Video;

public class TrendingVideoDTO {
    private Video video;
    private double trendingScore;

    public TrendingVideoDTO(Video video, double trendingScore) {
        this.video = video;
        this.trendingScore = trendingScore;
    }

    public Video getVideo() {
        return video;
    }

    public double getTrendingScore() {
        return trendingScore;
    }
}
