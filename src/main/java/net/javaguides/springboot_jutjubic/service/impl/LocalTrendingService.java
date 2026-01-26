package net.javaguides.springboot_jutjubic.service.impl;

import net.javaguides.springboot_jutjubic.config.LocalTrendingConfig;
import net.javaguides.springboot_jutjubic.dto.LocationDTO;
import net.javaguides.springboot_jutjubic.dto.TrendingVideoDTO;
import net.javaguides.springboot_jutjubic.model.Comment;
import net.javaguides.springboot_jutjubic.model.Video;
import net.javaguides.springboot_jutjubic.model.VideoLike;
import net.javaguides.springboot_jutjubic.model.VideoView;
import net.javaguides.springboot_jutjubic.repository.CommentRepository;
import net.javaguides.springboot_jutjubic.repository.VideoLikeRepository;
import net.javaguides.springboot_jutjubic.repository.VideoRepository;
import net.javaguides.springboot_jutjubic.repository.VideoViewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LocalTrendingService {

    private static final Logger logger = LoggerFactory.getLogger(LocalTrendingService.class);

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private VideoLikeRepository videoLikeRepository;

    @Autowired
    private VideoViewRepository videoViewRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LocalTrendingConfig config;

    //  CACHE SYSTEM

    private final Map<String, CachedResult> cache = new ConcurrentHashMap<>();

    private static class CachedResult {
        TrendingResult data;
        Instant cachedAt;
        long ttlSeconds;
        String strategy;

        CachedResult(TrendingResult data, long ttlSeconds, String strategy) {
            this.data = data;
            this.cachedAt = Instant.now();
            this.ttlSeconds = ttlSeconds;
            this.strategy = strategy;
        }

        boolean isValid() {
            return Duration.between(cachedAt, Instant.now()).getSeconds() < ttlSeconds;
        }
    }

    // PERFORMANCE TRACKING

    private final Map<String, List<Long>> responseTimeHistory = new ConcurrentHashMap<>();
    private final Map<String, PerformanceStats> performanceStats = new ConcurrentHashMap<>();
    private int totalRequests = 0;
    private int cacheHits = 0;
    private int cacheMisses = 0;

    private static class PerformanceStats {
        private final List<Double> responseTimes = new ArrayList<>();
        private int cacheHits = 0;
        private double sum = 0;
        private double min = Double.MAX_VALUE;
        private double max = 0;

        void addMeasurement(double responseTimeMs, boolean cacheHit) {
            responseTimes.add(responseTimeMs);
            sum += responseTimeMs;
            min = Math.min(min, responseTimeMs);
            max = Math.max(max, responseTimeMs);
            if (cacheHit) cacheHits++;
        }

        double getAverageResponseTime() {
            return responseTimes.isEmpty() ? 0 : sum / responseTimes.size();
        }

        double getMinResponseTime() {
            return responseTimes.isEmpty() ? 0 : min;
        }

        double getMaxResponseTime() {
            return responseTimes.isEmpty() ? 0 : max;
        }

        double getMedianResponseTime() {
            if (responseTimes.isEmpty()) return 0;
            List<Double> sorted = new ArrayList<>(responseTimes);
            Collections.sort(sorted);
            int middle = sorted.size() / 2;
            return sorted.size() % 2 == 0
                    ? (sorted.get(middle - 1) + sorted.get(middle)) / 2.0
                    : sorted.get(middle);
        }

        double getP95ResponseTime() {
            if (responseTimes.isEmpty()) return 0;
            List<Double> sorted = new ArrayList<>(responseTimes);
            Collections.sort(sorted);
            int index = (int) Math.ceil(sorted.size() * 0.95) - 1;
            return sorted.get(Math.max(0, index));
        }

        int getTotalRequests() {
            return responseTimes.size();
        }

        int getCacheHits() {
            return cacheHits;
        }

        double getCacheHitRate() {
            return responseTimes.isEmpty() ? 0 : (double) cacheHits / responseTimes.size() * 100;
        }
    }

    //  PUBLIC API

    /**
     * MAIN METHOD - Cached trending with 60s TTL
     */
    public TrendingResult getLocalTrending(LocationDTO userLocation, int radiusKm, int limit) {
        return getCachedTrending60s(userLocation, radiusKm, limit);
    }

    /**
     * STRATEGY 1: Real-time (no cache)
     */
    public TrendingResult getRealTimeTrending(LocationDTO userLocation, int radiusKm, int limit) {
        long startTime = System.nanoTime();
        try {
            TrendingResult result = calculateLocalTrending(userLocation, radiusKm, limit);
            recordMetrics("REAL_TIME", startTime, false);
            return result;
        } catch (Exception e) {
            recordMetrics("REAL_TIME", startTime, false);
            throw e;
        }
    }

    /**
     * STRATEGY 2: Cached with 30s TTL
     */
    public TrendingResult getCachedTrending30s(LocationDTO userLocation, int radiusKm, int limit) {
        return getCachedTrending(userLocation, radiusKm, limit, 30, "CACHED_30S");
    }

    /**
     * STRATEGY 3: Cached with 60s TTL
     */
    public TrendingResult getCachedTrending60s(LocationDTO userLocation, int radiusKm, int limit) {
        return getCachedTrending(userLocation, radiusKm, limit, 60, "CACHED_60S");
    }

    /**
     * STRATEGY 4: Cached with 5min TTL
     */
    public TrendingResult getCachedTrending5min(LocationDTO userLocation, int radiusKm, int limit) {
        return getCachedTrending(userLocation, radiusKm, limit, 300, "CACHED_5MIN");
    }

    // CACHE LOGIC

    private TrendingResult getCachedTrending(LocationDTO userLocation, int radiusKm, int limit,
                                             long ttlSeconds, String strategyName) {
        long startTime = System.nanoTime();

        String cacheKey = generateCacheKey(userLocation, radiusKm, limit, ttlSeconds);
        CachedResult cached = cache.get(cacheKey);

        if (cached != null && cached.isValid()) {
            cacheHits++;
            recordMetrics(strategyName, startTime, true);
            logger.info(" Cache HIT: {}", strategyName);
            return cached.data;
        }

        cacheMisses++;
        logger.info(" Cache MISS: {}", strategyName);

        try {
            TrendingResult result = calculateLocalTrending(userLocation, radiusKm, limit);
            cache.put(cacheKey, new CachedResult(result, ttlSeconds, strategyName));
            recordMetrics(strategyName, startTime, false);
            return result;
        } catch (Exception e) {
            recordMetrics(strategyName, startTime, false);
            throw e;
        }
    }

    private String generateCacheKey(LocationDTO location, int radius, int limit, long ttl) {
        return String.format("trending_%.4f_%.4f_%d_%d_%d",
                location.getLatitude(),
                location.getLongitude(),
                radius,
                limit,
                ttl);
    }

    // CORE CALCULATION

    private TrendingResult calculateLocalTrending(LocationDTO userLocation, int radiusKm, int limit) {
        long startTime = System.currentTimeMillis();

        try {
            SpatialIndex spatialIndex = buildSpatialIndex();

            List<SpatialObject> nearbyInteractions = spatialIndex.queryRadius(
                    userLocation.getLatitude(),
                    userLocation.getLongitude(),
                    radiusKm
            );

            Map<Long, VideoTrendingData> videoScores = new HashMap<>();

            for (SpatialObject obj : nearbyInteractions) {
                VideoTrendingData data = videoScores.computeIfAbsent(
                        obj.videoId,
                        k -> new VideoTrendingData()
                );

                double weight = obj.isApproximated ? config.getIpWeight() : config.getGpsWeight();

                switch (obj.type) {
                    case LIKE:
                        data.localLikes += weight;
                        break;
                    case VIEW:
                        data.localViews += weight;
                        break;
                    case COMMENT:
                        data.localComments += weight;
                        break;
                }
            }

            List<Video> allVideos = videoRepository.findAll();
            List<TrendingVideoDTO> trendingVideos = new ArrayList<>();

            for (Video video : allVideos) {
                VideoTrendingData data = videoScores.get(video.getId());

                if (data != null && data.totalInteractions() > 0) {
                    double score = (data.localViews * config.getViewWeight())
                            + (data.localLikes * config.getLikeWeight())
                            + (data.localComments * config.getCommentWeight());

                    data.totalScore = score;

                    TrendingVideoDTO dto = new TrendingVideoDTO(video, score);
                    dto.setViewCount((long) data.localViews);
                    dto.setLocalLikes(data.localLikes);
                    dto.setLocalViews(data.localViews);
                    dto.setLocalComments(data.localComments);

                    trendingVideos.add(dto);
                }
            }

            trendingVideos.sort((a, b) -> Double.compare(
                    b.getTrendingScore(), a.getTrendingScore()
            ));

            List<TrendingVideoDTO> result = new ArrayList<>();
            for (int i = 0; i < Math.min(limit, trendingVideos.size()); i++) {
                result.add(trendingVideos.get(i));
            }

            long elapsedMs = System.currentTimeMillis() - startTime;

            logger.info("Found {} trending videos in {} ms", result.size(), elapsedMs);

            return new TrendingResult(result, elapsedMs,
                    userLocation.getIsApproximated(), userLocation, radiusKm);

        } catch (Exception e) {
            long elapsedMs = System.currentTimeMillis() - startTime;
            logger.error("Error calculating trending: {}", e.getMessage(), e);
            return new TrendingResult(new ArrayList<>(), elapsedMs, true, userLocation, radiusKm);
        }
    }

    // SPATIAL INDEX BUILD

    private SpatialIndex buildSpatialIndex() {
        double minLat = config.getRegionMinLat();
        double maxLat = config.getRegionMaxLat();
        double minLng = config.getRegionMinLng();
        double maxLng = config.getRegionMaxLng();

        SpatialIndex index = new SpatialIndex(minLat, maxLat, minLng, maxLng, config);

        List<VideoLike> allLikes = videoLikeRepository.findAll();
        for (VideoLike like : allLikes) {
            if (like.getLatitude() != null && like.getLongitude() != null) {
                index.insert(new SpatialObject(
                        like.getVideoId(),
                        like.getLatitude(),
                        like.getLongitude(),
                        InteractionType.LIKE,
                        Boolean.TRUE.equals(like.getIsLocationApproximated())
                ));
            }
        }

        List<VideoView> allViews = videoViewRepository.findAll();
        for (VideoView view : allViews) {
            if (view.getLatitude() != null && view.getLongitude() != null) {
                index.insert(new SpatialObject(
                        view.getVideoId(),
                        view.getLatitude(),
                        view.getLongitude(),
                        InteractionType.VIEW,
                        Boolean.TRUE.equals(view.getIsLocationApproximated())
                ));
            }
        }

        List<Comment> allComments = commentRepository.findAll();
        for (Comment comment : allComments) {
            if (comment.getLatitude() != null && comment.getLongitude() != null) {
                index.insert(new SpatialObject(
                        comment.getVideo().getId(),
                        comment.getLatitude(),
                        comment.getLongitude(),
                        InteractionType.COMMENT,
                        Boolean.TRUE.equals(comment.getIsLocationApproximated())
                ));
            }
        }
        return index;
    }

    //  QUADTREE IMPLEMENTATION

    private static class SpatialIndex {
        private final QuadtreeNode root;
        private final LocalTrendingConfig config;

        public SpatialIndex(double minLat, double maxLat, double minLng, double maxLng, LocalTrendingConfig config) {
            this.config = config;
            this.root = new QuadtreeNode(minLat, maxLat, minLng, maxLng, config);
        }

        public void insert(SpatialObject obj) {
            root.insert(obj);
        }

        public List<SpatialObject> queryRadius(double lat, double lng, double radiusKm) {
            double latDelta = radiusKm / config.getDegreesToKmLatitude();
            double lngDelta = radiusKm / (config.getDegreesToKmLatitude() * Math.cos(Math.toRadians(lat)));

            double queryMinLat = lat - latDelta;
            double queryMaxLat = lat + latDelta;
            double queryMinLng = lng - lngDelta;
            double queryMaxLng = lng + lngDelta;

            List<SpatialObject> candidates = new ArrayList<>();
            root.query(queryMinLat, queryMaxLat, queryMinLng, queryMaxLng, candidates);

            List<SpatialObject> result = new ArrayList<>();
            for (SpatialObject obj : candidates) {
                double distance = calculateDistance(lat, lng, obj.lat, obj.lng);
                if (distance <= radiusKm) {
                    result.add(obj);
                }
            }

            return result;
        }

        private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
            final double R = config.getEarthRadiusKm();

            double latDistance = Math.toRadians(lat2 - lat1);
            double lngDistance = Math.toRadians(lng2 - lng1);

            double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                    + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                    * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            return R * c;
        }
    }

    private static class QuadtreeNode {
        private final int MAX_CAPACITY;
        private final int MAX_DEPTH;

        private final double minLat, maxLat, minLng, maxLng;
        private final List<SpatialObject> objects = new ArrayList<>();
        private final LocalTrendingConfig config;

        private QuadtreeNode nw, ne, sw, se;
        private boolean isDivided = false;

        public QuadtreeNode(double minLat, double maxLat, double minLng, double maxLng, LocalTrendingConfig config) {
            this.minLat = minLat;
            this.maxLat = maxLat;
            this.minLng = minLng;
            this.maxLng = maxLng;
            this.config = config;
            this.MAX_CAPACITY = config.getQuadtreeMaxCapacity();
            this.MAX_DEPTH = config.getQuadtreeMaxDepth();
        }

        public void insert(SpatialObject obj) {
            insert(obj, 0);
        }

        private void insert(SpatialObject obj, int depth) {
            if (obj.lat < minLat || obj.lat > maxLat || obj.lng < minLng || obj.lng > maxLng) {
                return;
            }

            if (objects.size() < MAX_CAPACITY || depth >= MAX_DEPTH) {
                objects.add(obj);
                return;
            }

            if (!isDivided) {
                subdivide();
            }

            double midLat = (minLat + maxLat) / 2.0;
            double midLng = (minLng + maxLng) / 2.0;

            if (obj.lat <= midLat) {
                if (obj.lng <= midLng) {
                    sw.insert(obj, depth + 1);
                } else {
                    se.insert(obj, depth + 1);
                }
            } else {
                if (obj.lng <= midLng) {
                    nw.insert(obj, depth + 1);
                } else {
                    ne.insert(obj, depth + 1);
                }
            }
        }

        private void subdivide() {
            double midLat = (minLat + maxLat) / 2.0;
            double midLng = (minLng + maxLng) / 2.0;

            nw = new QuadtreeNode(midLat, maxLat, minLng, midLng, config);
            ne = new QuadtreeNode(midLat, maxLat, midLng, maxLng, config);
            sw = new QuadtreeNode(minLat, midLat, minLng, midLng, config);
            se = new QuadtreeNode(minLat, midLat, midLng, maxLng, config);

            isDivided = true;

            for (SpatialObject obj : objects) {
                if (obj.lat <= midLat) {
                    if (obj.lng <= midLng) {
                        sw.objects.add(obj);
                    } else {
                        se.objects.add(obj);
                    }
                } else {
                    if (obj.lng <= midLng) {
                        nw.objects.add(obj);
                    } else {
                        ne.objects.add(obj);
                    }
                }
            }
            objects.clear();
        }

        public void query(double qMinLat, double qMaxLat, double qMinLng, double qMaxLng,
                          List<SpatialObject> result) {
            if (qMaxLat < minLat || qMinLat > maxLat || qMaxLng < minLng || qMinLng > maxLng) {
                return;
            }

            for (SpatialObject obj : objects) {
                if (obj.lat >= qMinLat && obj.lat <= qMaxLat &&
                        obj.lng >= qMinLng && obj.lng <= qMaxLng) {
                    result.add(obj);
                }
            }

            if (isDivided) {
                nw.query(qMinLat, qMaxLat, qMinLng, qMaxLng, result);
                ne.query(qMinLat, qMaxLat, qMinLng, qMaxLng, result);
                sw.query(qMinLat, qMaxLat, qMinLng, qMaxLng, result);
                se.query(qMinLat, qMaxLat, qMinLng, qMaxLng, result);
            }
        }
    }

    // DATA CLASSES

    private enum InteractionType {
        LIKE, VIEW, COMMENT
    }

    private static class SpatialObject {
        final Long videoId;
        final double lat;
        final double lng;
        final InteractionType type;
        final boolean isApproximated;

        public SpatialObject(Long videoId, double lat, double lng,
                             InteractionType type, boolean isApproximated) {
            this.videoId = videoId;
            this.lat = lat;
            this.lng = lng;
            this.type = type;
            this.isApproximated = isApproximated;
        }
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

    // METRICS

    private void recordMetrics(String strategy, long startTimeNanos, boolean cacheHit) {
        totalRequests++;
        long responseTimeNanos = System.nanoTime() - startTimeNanos;
        double responseTimeMs = responseTimeNanos / 1_000_000.0;

        responseTimeHistory.computeIfAbsent(strategy, k -> new ArrayList<>()).add((long) responseTimeMs);

        PerformanceStats stats = performanceStats.computeIfAbsent(strategy, k -> new PerformanceStats());
        stats.addMeasurement(responseTimeMs, cacheHit);

        logger.info("Strategy: {} | Response: {:.2f}ms | Cache Hit: {}", strategy, responseTimeMs, cacheHit);
    }

    public PerformanceMetrics getMetrics() {
        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setTotalRequests(totalRequests);
        metrics.setCacheHits(cacheHits);
        metrics.setCacheMisses(cacheMisses);
        metrics.setCacheHitRate(totalRequests > 0 ? (double) cacheHits / totalRequests * 100 : 0);

        Map<String, StrategyMetrics> strategyMetrics = new HashMap<>();

        for (Map.Entry<String, PerformanceStats> entry : performanceStats.entrySet()) {
            StrategyMetrics sm = new StrategyMetrics();
            PerformanceStats stats = entry.getValue();

            sm.setAverageResponseTime(stats.getAverageResponseTime());
            sm.setMinResponseTime(stats.getMinResponseTime());
            sm.setMaxResponseTime(stats.getMaxResponseTime());
            sm.setMedianResponseTime(stats.getMedianResponseTime());
            sm.setP95ResponseTime(stats.getP95ResponseTime());
            sm.setTotalRequests(stats.getTotalRequests());
            sm.setCacheHits(stats.getCacheHits());
            sm.setCacheHitRate(stats.getCacheHitRate());

            strategyMetrics.put(entry.getKey(), sm);
        }

        metrics.setStrategies(strategyMetrics);
        metrics.setResponseTimeHistory(responseTimeHistory);

        return metrics;
    }

    public void resetMetrics() {
        responseTimeHistory.clear();
        performanceStats.clear();
        cache.clear();
        totalRequests = 0;
        cacheHits = 0;
        cacheMisses = 0;
        logger.info(" Metrics reset");
    }

    // DTO CLASSES

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
        public void setVideos(List<TrendingVideoDTO> videos) { this.videos = videos; }

        public long getResponseTimeMs() { return responseTimeMs; }
        public void setResponseTimeMs(long responseTimeMs) { this.responseTimeMs = responseTimeMs; }

        public boolean isLocationApproximated() { return isLocationApproximated; }
        public void setLocationApproximated(boolean locationApproximated) {
            isLocationApproximated = locationApproximated;
        }

        public LocationDTO getUserLocation() { return userLocation; }
        public void setUserLocation(LocationDTO userLocation) { this.userLocation = userLocation; }

        public int getRadiusKm() { return radiusKm; }
        public void setRadiusKm(int radiusKm) { this.radiusKm = radiusKm; }
    }

    public static class PerformanceMetrics {
        private int totalRequests;
        private int cacheHits;
        private int cacheMisses;
        private double cacheHitRate;
        private Map<String, StrategyMetrics> strategies;
        private Map<String, List<Long>> responseTimeHistory;

        public int getTotalRequests() { return totalRequests; }
        public void setTotalRequests(int totalRequests) { this.totalRequests = totalRequests; }

        public int getCacheHits() { return cacheHits; }
        public void setCacheHits(int cacheHits) { this.cacheHits = cacheHits; }

        public int getCacheMisses() { return cacheMisses; }
        public void setCacheMisses(int cacheMisses) { this.cacheMisses = cacheMisses; }

        public double getCacheHitRate() { return cacheHitRate; }
        public void setCacheHitRate(double cacheHitRate) { this.cacheHitRate = cacheHitRate; }

        public Map<String, StrategyMetrics> getStrategies() { return strategies; }
        public void setStrategies(Map<String, StrategyMetrics> strategies) {
            this.strategies = strategies;
        }

        public Map<String, List<Long>> getResponseTimeHistory() { return responseTimeHistory; }
        public void setResponseTimeHistory(Map<String, List<Long>> responseTimeHistory) {
            this.responseTimeHistory = responseTimeHistory;
        }
    }

    public static class StrategyMetrics {
        private double averageResponseTime;
        private double minResponseTime;
        private double maxResponseTime;
        private double medianResponseTime;
        private double p95ResponseTime;
        private int totalRequests;
        private int cacheHits;
        private double cacheHitRate;

        public double getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(double averageResponseTime) {
            this.averageResponseTime = averageResponseTime;
        }

        public double getMinResponseTime() { return minResponseTime; }
        public void setMinResponseTime(double minResponseTime) {
            this.minResponseTime = minResponseTime;
        }

        public double getMaxResponseTime() { return maxResponseTime; }
        public void setMaxResponseTime(double maxResponseTime) {
            this.maxResponseTime = maxResponseTime;
        }

        public double getMedianResponseTime() { return medianResponseTime; }
        public void setMedianResponseTime(double medianResponseTime) {
            this.medianResponseTime = medianResponseTime;
        }

        public double getP95ResponseTime() { return p95ResponseTime; }
        public void setP95ResponseTime(double p95ResponseTime) {
            this.p95ResponseTime = p95ResponseTime;
        }

        public int getTotalRequests() { return totalRequests; }
        public void setTotalRequests(int totalRequests) { this.totalRequests = totalRequests; }

        public int getCacheHits() { return cacheHits; }
        public void setCacheHits(int cacheHits) { this.cacheHits = cacheHits; }

        public double getCacheHitRate() { return cacheHitRate; }
        public void setCacheHitRate(double cacheHitRate) { this.cacheHitRate = cacheHitRate; }
    }
}