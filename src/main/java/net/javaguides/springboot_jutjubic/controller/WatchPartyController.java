package net.javaguides.springboot_jutjubic.controller;

import net.javaguides.springboot_jutjubic.dto.WatchPartyCommandDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class WatchPartyController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @MessageMapping("/watchparty.createRoom")
    public void createRoom(WatchPartyCommandDTO command) {
        logger.info("Kreirana Watch Party soba: {} od strane korisnika {}",
                command.getRoomId(), command.getUserId());


        messagingTemplate.convertAndSend(
                "/topic/watchparty/" + command.getRoomId(),
                "Soba " + command.getRoomId() + " je kreirana!"
        );
    }


    @MessageMapping("/watchparty.join")
    public void joinRoom(WatchPartyCommandDTO command) {
        logger.info("Korisnik {} ušao u Watch Party sobu {}",
                command.getUserId(), command.getRoomId());


        messagingTemplate.convertAndSend(
                "/topic/watchparty/" + command.getRoomId(),
                command.getUserId() + " se pridružio watch party-ju"
        );
    }


    @MessageMapping("/watchparty.playVideo")
    public void playVideo(WatchPartyCommandDTO command) {
        logger.info("Video {} pokrenut u sobi {} od strane {}",
                command.getVideoId(), command.getRoomId(), command.getUserId());

        command.setAction("play");

        messagingTemplate.convertAndSend(
                "/topic/watchparty/" + command.getRoomId(),
                command
        );
    }


    @MessageMapping("/watchparty.leave")
    public void leaveRoom(WatchPartyCommandDTO command) {
        logger.info("Korisnik {} napustio Watch Party sobu {}",
                command.getUserId(), command.getRoomId());

        messagingTemplate.convertAndSend(
                "/topic/watchparty/" + command.getRoomId(),
                command.getUserId() + " je napustio watch party"
        );
    }
}