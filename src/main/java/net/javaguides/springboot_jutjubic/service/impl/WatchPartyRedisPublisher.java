package net.javaguides.springboot_jutjubic.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.javaguides.springboot_jutjubic.config.RedisConfig;
import net.javaguides.springboot_jutjubic.dto.WatchPartyEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class WatchPartyRedisPublisher {

    private static final Logger logger = LoggerFactory.getLogger(WatchPartyRedisPublisher.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void publish(WatchPartyEventDTO event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            redisTemplate.convertAndSend(RedisConfig.WATCH_PARTY_CHANNEL, message);
            logger.info("Published to Redis: roomId={}, type={}", event.getRoomId(), event.getEventType());
        } catch (Exception e) {
            logger.error("Failed to publish to Redis: {}", e.getMessage(), e);
        }
    }
}