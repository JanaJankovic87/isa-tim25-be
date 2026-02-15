package net.javaguides.springboot_jutjubic.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.javaguides.springboot_jutjubic.dto.WatchPartyEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WatchPartyRedisSubscriber implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(WatchPartyRedisSubscriber.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody());
            WatchPartyEventDTO event = objectMapper.readValue(body, WatchPartyEventDTO.class);

            String destination = "/topic/watch-party/" + event.getRoomId();
            messagingTemplate.convertAndSend(destination, event);

            logger.info("Forwarded {} to WebSocket {}", event.getEventType(), destination);
        } catch (Exception e) {
            logger.error("Failed to process Redis message: {}", e.getMessage(), e);
        }
    }
}