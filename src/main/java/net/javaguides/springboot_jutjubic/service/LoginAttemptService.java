package net.javaguides.springboot_jutjubic.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

public interface LoginAttemptService {

    @Cacheable(value = "loginAttempts", unless = "#result == null")
    Integer getAttempts(String ipAddress);

    @CachePut(value = "loginAttempts", key = "#ipAddress")
    Integer updateAttempts(String ipAddress, Integer attempts);

    void recordAttempt(String ipAddress, boolean success);

    boolean isBlocked(String ipAddress);

    int getRemainingAttempts(String ipAddress);

    @CacheEvict(cacheNames = {"loginAttempts"}, allEntries = true)
    void removeFromCache();

    @CacheEvict(cacheNames = {"loginAttempts"}, key = "#ipAddress")
    void resetAttempts(String ipAddress);
}
