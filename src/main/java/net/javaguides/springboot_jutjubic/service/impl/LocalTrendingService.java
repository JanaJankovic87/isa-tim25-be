package net.javaguides.springboot_jutjubic.service.impl;


import net.javaguides.springboot_jutjubic.model.Comment;
import net.javaguides.springboot_jutjubic.model.VideoLike;
import net.javaguides.springboot_jutjubic.model.VideoView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import net.javaguides.springboot_jutjubic.dto.LocationDTO;
import net.javaguides.springboot_jutjubic.dto.TrendingVideoDTO;
import net.javaguides.springboot_jutjubic.model.Video;
import net.javaguides.springboot_jutjubic.service.VideoService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class LocalTrendingService {

    private static final Logger logger = LoggerFactory.getLogger(LocalTrendingService.class);

    @Autowired
    private GeolocationService geolocationService;

    @Autowired
    private VideoService videoService;

    private long totalRequests = 0;
    private long totalResponseTimeMs = 0;
    private final List<Long> responseTimes = new ArrayList<>();

    public TrendingResult getLocalTrending(LocationDTO userLocation, int radiusKm, int limit) {
        long startTime = System.currentTimeMillis();

        try {

            List<Video> allVideos = videoService.findAll();
            List<TrendingVideoDTO> nearbyVideos = new ArrayList<>();

            for (Video video : allVideos) {

                VideoTrendingData data = calculateLocalTrendingScore(video, userLocation, radiusKm);

                if (data.totalScore > 0) {
                    TrendingVideoDTO dto = new TrendingVideoDTO(video, data.totalScore);
                    dto.setViewCount((long) data.localViews);

                    dto.setLocalLikes(data.localLikes);
                    dto.setLocalViews(data.localViews);
                    dto.setLocalComments(data.localComments);

                    nearbyVideos.add(dto);
                }
            }

            nearbyVideos.sort((a, b) -> Double.compare(
                    b.getTrendingScore(),
                    a.getTrendingScore()
            ));

            List<TrendingVideoDTO> result = new ArrayList<>();
            int maxResults = Math.min(5, nearbyVideos.size());
            for (int i = 0; i < maxResults; i++) {
                result.add(nearbyVideos.get(i));
            }

            long elapsedMs = System.currentTimeMillis() - startTime;
            updateMetrics(elapsedMs);

            return new TrendingResult(
                    result,
                    elapsedMs,
                    userLocation.getIsApproximated(),
                    userLocation,
                    radiusKm
            );
        } catch (Exception e) {
            long elapsedMs = System.currentTimeMillis() - startTime;
            return new TrendingResult(
                    new ArrayList<>(),
                    elapsedMs,
                    true,
                    userLocation,
                    radiusKm
            );
        }
    }

    private VideoTrendingData calculateLocalTrendingScore(Video video, LocationDTO userLocation, int radiusKm) {
        VideoTrendingData data = new VideoTrendingData();

        List<VideoLike> allLikes = videoService.getAllVideoLikes(video.getId());
        for (VideoLike like : allLikes) {
            if (isWithinRadius(userLocation, like.getLatitude(), like.getLongitude(), radiusKm)) {
                if (Boolean.TRUE.equals(like.getIsLocationApproximated())) {
                    data.localLikes += 0.5;
                } else {
                    data.localLikes += 1.0;
                }
            }
        }

        List<VideoView> allViews = videoService.getAllViews(video.getId());
        for (VideoView view : allViews) {
            if (isWithinRadius(userLocation, view.getLatitude(), view.getLongitude(), radiusKm)) {
                if (Boolean.TRUE.equals(view.getIsLocationApproximated())) {
                    data.localViews += 0.5;
                } else {
                    data.localViews += 1.0;
                }
            }
        }

        List<Comment> allComments = videoService.getAllComments(video.getId());
        for (Comment comment : allComments) {
            if (isWithinRadius(userLocation, comment.getLatitude(), comment.getLongitude(), radiusKm)) {
                if (Boolean.TRUE.equals(comment.getIsLocationApproximated())) {
                    data.localComments += 0.5;
                } else {
                    data.localComments += 1.0;
                }
            }
        }

//        // dodatno za novije videe
//        long hoursSinceUpload = ChronoUnit.HOURS.between(video.getCreatedAt(), LocalDateTime.now());
//        double freshness = 1.0 / (1 + hoursSinceUpload / 24.0);

        // Views × 0.4 + Likes × 0.3 + Comments × 0.2 + Freshness × 0.1
        data.totalScore = (data.localViews * 0.4)
                + (data.localLikes * 0.3)
                + (data.localComments * 0.2);
//                + (freshness * 0.1);

        return data;
    }

    /**
     * Provera da li je lokacija unutar radijusa
     */
    private boolean isWithinRadius(LocationDTO userLoc, Double lat, Double lng, int radiusKm) {
        // Ako nema koordinata interakcije, preskoči
        if (userLoc == null || lat == null || lng == null) {
            return false;
        }

        double distance = geolocationService.calculateDistance(
                userLoc.getLatitude(),
                userLoc.getLongitude(),
                lat,
                lng
        );

        return distance <= radiusKm;
    }

    private synchronized void updateMetrics(long responseTimeMs) {
        totalRequests++;
        totalResponseTimeMs += responseTimeMs;
        responseTimes.add(responseTimeMs);

        if (responseTimes.size() > 1000) {
            responseTimes.remove(0);
        }
    }

    public PerformanceMetrics getMetrics() {
        synchronized (this) {
            if (totalRequests == 0) {
                return new PerformanceMetrics(0, 0, 0, 0, 0);
            }

            double avgResponseTime = (double) totalResponseTimeMs / totalRequests;

            List<Long> sortedTimes = new ArrayList<>(responseTimes);
            sortedTimes.sort(Long::compareTo);

            long minTime = sortedTimes.get(0);
            long maxTime = sortedTimes.get(sortedTimes.size() - 1);

            int p95Index = (int) Math.ceil(sortedTimes.size() * 0.95) - 1;
            long p95Time = sortedTimes.get(Math.max(0, p95Index));

            return new PerformanceMetrics(totalRequests, avgResponseTime, minTime, maxTime, p95Time);
        }
    }

    public synchronized void resetMetrics() {
        totalRequests = 0;
        totalResponseTimeMs = 0;
        responseTimes.clear();
    }

    public static class TrendingResult {
        private List<TrendingVideoDTO> videos;
        private long responseTimeMs;
        private boolean isLocationApproximated;
        private LocationDTO userLocation;
        private int radiusKm;

        public TrendingResult(List<TrendingVideoDTO> videos, long responseTimeMs,
                              boolean isLocationApproximated, LocationDTO userLocation, int radiusKm) {
            this.videos = videos;
            this.responseTimeMs = responseTimeMs;
            this.isLocationApproximated = isLocationApproximated;
            this.userLocation = userLocation;
            this.radiusKm = radiusKm;
        }

        public List<TrendingVideoDTO> getVideos() { return videos; }
        public long getResponseTimeMs() { return responseTimeMs; }
        public boolean isLocationApproximated() { return isLocationApproximated; }
        public LocationDTO getUserLocation() { return userLocation; }
        public int getRadiusKm() { return radiusKm; }
    }

    public static class PerformanceMetrics {
        private long totalRequests;
        private double avgResponseTimeMs;
        private long minResponseTimeMs;
        private long maxResponseTimeMs;
        private long p95ResponseTimeMs;

        public PerformanceMetrics(long totalRequests, double avgResponseTimeMs,
                                  long minResponseTimeMs, long maxResponseTimeMs, long p95ResponseTimeMs) {
            this.totalRequests = totalRequests;
            this.avgResponseTimeMs = avgResponseTimeMs;
            this.minResponseTimeMs = minResponseTimeMs;
            this.maxResponseTimeMs = maxResponseTimeMs;
            this.p95ResponseTimeMs = p95ResponseTimeMs;
        }

        public long getTotalRequests() { return totalRequests; }
        public double getAvgResponseTimeMs() { return avgResponseTimeMs; }
        public long getMinResponseTimeMs() { return minResponseTimeMs; }
        public long getMaxResponseTimeMs() { return maxResponseTimeMs; }
        public long getP95ResponseTimeMs() { return p95ResponseTimeMs; }
    }

    private static class VideoTrendingData {
        double localLikes = 0;
        double localViews = 0;
        double localComments = 0;
        double totalScore = 0;

        int totalInteractions() {
            return (int) (localLikes + localViews + localComments);
        }
    }
}