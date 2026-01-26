package net.javaguides.springboot_jutjubic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.trending")
public class LocalTrendingConfig {

    // Geografske granice
    private double regionMinLat = 36.0;  // Jug Balkana
    private double regionMaxLat = 47.0;  // Sever Balkana
    private double regionMinLng = 13.0;  // Zapad Balkana
    private double regionMaxLng = 30.0;  // Istok Balkana

    // Quadtree parametri
    private int quadtreeMaxCapacity = 50;
    private int quadtreeMaxDepth = 8;

    // Default radijus i limit za pretragu
    private int defaultRadiusKm = 50;
    private int defaultLimit = 5;

    // Težine za scoring algoritam
    private double viewWeight = 0.4;
    private double likeWeight = 0.3;
    private double commentWeight = 0.2;

    // Težine za GPS vs IP lokaciju
    private double gpsWeight = 1.0;
    private double ipWeight = 0.5;

    // Performance tracking
    private int maxResponseTimeSamples = 1000;

    // Earth radius u km (za Haversine formulu)
    private double earthRadiusKm = 6371.0;

    // Konverzija stepena u km
    private double degreesToKmLatitude = 111.0;

    // Getters and Setters

    public double getRegionMinLat() {
        return regionMinLat;
    }

    public void setRegionMinLat(double regionMinLat) {
        this.regionMinLat = regionMinLat;
    }

    public double getRegionMaxLat() {
        return regionMaxLat;
    }

    public void setRegionMaxLat(double regionMaxLat) {
        this.regionMaxLat = regionMaxLat;
    }

    public double getRegionMinLng() {
        return regionMinLng;
    }

    public void setRegionMinLng(double regionMinLng) {
        this.regionMinLng = regionMinLng;
    }

    public double getRegionMaxLng() {
        return regionMaxLng;
    }

    public void setRegionMaxLng(double regionMaxLng) {
        this.regionMaxLng = regionMaxLng;
    }

    public int getQuadtreeMaxCapacity() {
        return quadtreeMaxCapacity;
    }

    public void setQuadtreeMaxCapacity(int quadtreeMaxCapacity) {
        this.quadtreeMaxCapacity = quadtreeMaxCapacity;
    }

    public int getQuadtreeMaxDepth() {
        return quadtreeMaxDepth;
    }

    public void setQuadtreeMaxDepth(int quadtreeMaxDepth) {
        this.quadtreeMaxDepth = quadtreeMaxDepth;
    }

    public int getDefaultRadiusKm() {
        return defaultRadiusKm;
    }

    public void setDefaultRadiusKm(int defaultRadiusKm) {
        this.defaultRadiusKm = defaultRadiusKm;
    }

    public int getDefaultLimit() {
        return defaultLimit;
    }

    public void setDefaultLimit(int defaultLimit) {
        this.defaultLimit = defaultLimit;
    }

    public double getViewWeight() {
        return viewWeight;
    }

    public void setViewWeight(double viewWeight) {
        this.viewWeight = viewWeight;
    }

    public double getLikeWeight() {
        return likeWeight;
    }

    public void setLikeWeight(double likeWeight) {
        this.likeWeight = likeWeight;
    }

    public double getCommentWeight() {
        return commentWeight;
    }

    public void setCommentWeight(double commentWeight) {
        this.commentWeight = commentWeight;
    }

    public double getGpsWeight() {
        return gpsWeight;
    }

    public void setGpsWeight(double gpsWeight) {
        this.gpsWeight = gpsWeight;
    }

    public double getIpWeight() {
        return ipWeight;
    }

    public void setIpWeight(double ipWeight) {
        this.ipWeight = ipWeight;
    }

    public int getMaxResponseTimeSamples() {
        return maxResponseTimeSamples;
    }

    public void setMaxResponseTimeSamples(int maxResponseTimeSamples) {
        this.maxResponseTimeSamples = maxResponseTimeSamples;
    }

    public double getEarthRadiusKm() {
        return earthRadiusKm;
    }

    public void setEarthRadiusKm(double earthRadiusKm) {
        this.earthRadiusKm = earthRadiusKm;
    }

    public double getDegreesToKmLatitude() {
        return degreesToKmLatitude;
    }

    public void setDegreesToKmLatitude(double degreesToKmLatitude) {
        this.degreesToKmLatitude = degreesToKmLatitude;
    }
}

