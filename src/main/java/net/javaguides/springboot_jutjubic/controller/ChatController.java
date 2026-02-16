package net.javaguides.springboot_jutjubic.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import net.javaguides.springboot_jutjubic.dto.ChatMessageDTO;

import java.time.LocalDateTime;

@Controller
public class ChatController {

    @MessageMapping("/chat/{videoId}")
    @SendTo("/topic/video/{videoId}")
    public ChatMessageDTO sendMessage(@DestinationVariable Long videoId, ChatMessageDTO message) {
        message.setVideoId(videoId);
        message.setTimestamp(LocalDateTime.now());
        return message;
    }
}