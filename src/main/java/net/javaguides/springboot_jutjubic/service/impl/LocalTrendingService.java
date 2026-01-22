package net.javaguides.springboot_jutjubic.service.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import net.javaguides.springboot_jutjubic.dto.LocationDTO;
import net.javaguides.springboot_jutjubic.dto.TrendingVideoDTO;
import net.javaguides.springboot_jutjubic.model.Video;
import net.javaguides.springboot_jutjubic.service.VideoService;
import java.util.ArrayList;
import java.util.List;

@Service
public class LocalTrendingService {

    private static final Logger logger = LoggerFactory.getLogger(LocalTrendingService.class);

    @Autowired
    private GeolocationService geolocationService;

    @Autowired
    private VideoService videoService;

    // S2: Performance tracking
    private long totalRequests = 0;
    private long totalResponseTimeMs = 0;
    private final List<Long> responseTimes = new ArrayList<>();

    public TrendingResult getLocalTrending(LocationDTO userLocation, int radiusKm, int limit) {
        long startTime = System.currentTimeMillis();

        try {
            logger.info("Finding videos near: {}, radius: {}km",
                    userLocation.getLocationName(), radiusKm);

            List<Video> allVideos = videoService.findAll();
            List<TrendingVideoDTO> nearbyVideos = new ArrayList<>();

            for (Video video : allVideos) {
                if (video.getLatitude() != null && video.getLongitude() != null) {
                    double distance = geolocationService.calculateDistance(
                            userLocation.getLatitude(),
                            userLocation.getLongitude(),
                            video.getLatitude(),
                            video.getLongitude()
                    );

                    if (distance <= radiusKm) {
                        double popularityScore = videoService.calculateTrendingScore(video);

                        TrendingVideoDTO dto = new TrendingVideoDTO(video, popularityScore, distance);
                        dto.setViewCount(videoService.getViewCount(video.getId()));
                        nearbyVideos.add(dto);
                    }
                }
            }

            nearbyVideos.sort((a, b) -> Double.compare(
                    b.getPopularityScore(),
                    a.getPopularityScore()
            ));

            List<TrendingVideoDTO> result = nearbyVideos.subList(
                    0,
                    Math.min(limit, nearbyVideos.size())
            );

            long elapsedMs = System.currentTimeMillis() - startTime;
            updateMetrics(elapsedMs);

            logger.info("✓ Found {} videos in {}ms", result.size(), elapsedMs);

            return new TrendingResult(result, elapsedMs, userLocation.getIsApproximated());

        } catch (Exception e) {
            long elapsedMs = System.currentTimeMillis() - startTime;
            logger.error("✗ Error: {}", e.getMessage());
            return new TrendingResult(new ArrayList<>(), elapsedMs, true);
        }
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

        public TrendingResult(List<TrendingVideoDTO> videos, long responseTimeMs, boolean isLocationApproximated) {
            this.videos = videos;
            this.responseTimeMs = responseTimeMs;
            this.isLocationApproximated = isLocationApproximated;
        }

        public List<TrendingVideoDTO> getVideos() { return videos; }
        public long getResponseTimeMs() { return responseTimeMs; }
        public boolean isLocationApproximated() { return isLocationApproximated; }
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
}