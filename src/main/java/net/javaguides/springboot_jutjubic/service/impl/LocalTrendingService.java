package net.javaguides.springboot_jutjubic.service.impl;

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

import java.util.*;


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
    private GeolocationService geolocationService;

    // Performance tracking
    private long totalRequests = 0;
    private long totalResponseTimeMs = 0;
    private final List<Long> responseTimes = new ArrayList<>();

    public TrendingResult getLocalTrending(LocationDTO userLocation, int radiusKm, int limit) {
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

                double weight = obj.isApproximated ? 0.5 : 1.0;

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

                    double score = (data.localViews * 0.4)
                            + (data.localLikes * 0.3)
                            + (data.localComments * 0.2);

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
            int count = 0;
            for (TrendingVideoDTO dto : trendingVideos) {
                if (count >= limit) break;
                result.add(dto);
                count++;
            }

            long elapsedMs = System.currentTimeMillis() - startTime;
            updateMetrics(elapsedMs);

            logger.info("Pronađeno {} trending videa u {} ms", result.size(), elapsedMs);

            return new TrendingResult(result, elapsedMs,
                    userLocation.getIsApproximated(), userLocation, radiusKm);

        } catch (Exception e) {
            long elapsedMs = System.currentTimeMillis() - startTime;
            logger.error("Greška: {}", e.getMessage(), e);
            return new TrendingResult(new ArrayList<>(), elapsedMs, true, userLocation, radiusKm);
        }
    }

    private SpatialIndex buildSpatialIndex() {

        // granice Balkana
        double minLat = 36;  // Jug
        double maxLat = 47;  // Sever
        double minLng = 13;  // Zapad
        double maxLng = 30;  // Istok

        SpatialIndex index = new SpatialIndex(minLat, maxLat, minLng, maxLng);

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

    private static class SpatialIndex {
        private final QuadtreeNode root;
        private final double minLat, maxLat, minLng, maxLng;

        public SpatialIndex(double minLat, double maxLat, double minLng, double maxLng) {
            this.minLat = minLat;
            this.maxLat = maxLat;
            this.minLng = minLng;
            this.maxLng = maxLng;
            this.root = new QuadtreeNode(minLat, maxLat, minLng, maxLng);
        }

        public void insert(SpatialObject obj) {
            root.insert(obj);
        }

        public List<SpatialObject> queryRadius(double lat, double lng, double radiusKm) {
            double latDelta = radiusKm / 111.0; // 1° latitude ≈ 111 km
            double lngDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));

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
            final int R = 6371; // precnik zemlje

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
        private static final int MAX_CAPACITY = 50;
        private static final int MAX_DEPTH = 8;

        private final double minLat, maxLat, minLng, maxLng;
        private final List<SpatialObject> objects = new ArrayList<>();

        private QuadtreeNode nw, ne, sw, se; // Children nodes
        private boolean isDivided = false;

        public QuadtreeNode(double minLat, double maxLat, double minLng, double maxLng) {
            this.minLat = minLat;
            this.maxLat = maxLat;
            this.minLng = minLng;
            this.maxLng = maxLng;
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

            nw = new QuadtreeNode(midLat, maxLat, minLng, midLng);
            ne = new QuadtreeNode(midLat, maxLat, midLng, maxLng);
            sw = new QuadtreeNode(minLat, midLat, minLng, midLng);
            se = new QuadtreeNode(minLat, midLat, midLng, maxLng);

            isDivided = true;

            // Redistribute existing objects
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
}

