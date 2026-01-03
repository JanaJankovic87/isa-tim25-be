package net.javaguides.springboot_jutjubic.service.impl;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.javaguides.springboot_jutjubic.service.LoginAttemptService;

@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private final Logger LOG = LoggerFactory.getLogger(LoginAttemptServiceImpl.class);

    private static final int MAX_ATTEMPTS = 5;

    @Override
    @Cacheable(value = "loginAttempts", key = "#ipAddress", unless = "#result == null")
    public Integer getAttempts(String ipAddress) {
        LOG.info("Cache miss for IP: " + ipAddress + " - returning null (will NOT be cached due to unless condition)");
        return null;
    }

    @Override
    @CachePut(value = "loginAttempts", key = "#ipAddress")
    public Integer updateAttempts(String ipAddress, Integer attempts) {
        LOG.info("Updating cache for IP: " + ipAddress + " with attempts: " + attempts);
        return attempts;
    }

    @Override
    public void recordAttempt(String ipAddress, boolean success) {
        if (success) {
            LOG.info("Successful login from IP: " + ipAddress + ". Resetting attempts.");
            resetAttempts(ipAddress);
        } else {
            Integer attempts = getAttempts(ipAddress);

            if (attempts == null) {
                LOG.info("First failed login attempt for IP: " + ipAddress);
                updateAttempts(ipAddress, 1);
            } else {
                LOG.info("Failed login attempt #" + (attempts + 1) + " for IP: " + ipAddress);
                updateAttempts(ipAddress, attempts + 1);
            }
        }
    }

    @Override
    public boolean isBlocked(String ipAddress) {
        Integer attempts = getAttempts(ipAddress);
        boolean blocked = attempts != null && attempts >= MAX_ATTEMPTS;

        if (blocked) {
            LOG.warn("IP " + ipAddress + " is BLOCKED! (attempts: " + attempts + ")");
        }

        return blocked;
    }

    @Override
    public int getRemainingAttempts(String ipAddress) {
        Integer attempts = getAttempts(ipAddress);

        if (attempts == null) {
            LOG.info("No attempts recorded for IP: " + ipAddress + ", returning MAX_ATTEMPTS: " + MAX_ATTEMPTS);
            return MAX_ATTEMPTS;
        }

        int remaining = Math.max(0, MAX_ATTEMPTS - attempts);
        LOG.info("Remaining attempts for IP: " + ipAddress + " = " + remaining + " (current attempts: " + attempts + ")");
        return remaining;
    }

    @Override
    @CacheEvict(value = "loginAttempts", key = "#ipAddress")
    public void resetAttempts(String ipAddress) {
        LOG.info("Reset attempts for IP: " + ipAddress + " (removed from cache)");
    }

    @Override
    @CacheEvict(value = "loginAttempts", allEntries = true)
    public void removeFromCache() {
        LOG.info("All login attempts removed from cache!");
    }
}