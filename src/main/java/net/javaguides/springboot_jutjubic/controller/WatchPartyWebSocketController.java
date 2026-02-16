package net.javaguides.springboot_jutjubic.controller;

import net.javaguides.springboot_jutjubic.dto.WatchPartyCommandDTO;
import net.javaguides.springboot_jutjubic.dto.WatchPartyEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WatchPartyWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WatchPartyWebSocketController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/watchparty.join")
    public void handleJoin(@Payload WatchPartyCommandDTO command) {
        logger.info("User joined room via WebSocket: roomId={}, userId={}",
                command.getRoomId(), command.getUserId());
    }

    @MessageMapping("/watchparty.leave")
    public void handleLeave(@Payload WatchPartyCommandDTO command) {
        logger.info("User left room via WebSocket: roomId={}, userId={}",
                command.getRoomId(), command.getUserId());
    }

    @MessageMapping("/watchparty.createRoom")
    public void handleCreateRoom(@Payload WatchPartyCommandDTO command) {
        logger.info("Room created via WebSocket: roomId={}", command.getRoomId());
    }
}