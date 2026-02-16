package net.javaguides.springboot_jutjubic.service.impl;

import net.javaguides.springboot_jutjubic.dto.WatchPartyEventDTO;
import net.javaguides.springboot_jutjubic.dto.WatchPartyRoomDTO;
import net.javaguides.springboot_jutjubic.model.WatchPartyRoom;
import net.javaguides.springboot_jutjubic.repository.WatchPartyRoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WatchPartyService {

    private static final Logger logger = LoggerFactory.getLogger(WatchPartyService.class);

    @Autowired
    private WatchPartyRoomRepository roomRepository;

    @Autowired
    private WatchPartyRedisPublisher redisPublisher;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public WatchPartyRoomDTO createRoom(Long ownerId, String ownerUsername) {
        WatchPartyRoom room = new WatchPartyRoom(ownerId, ownerUsername);
        WatchPartyRoom saved = roomRepository.save(room);
        logger.info("Room created in DB: roomId={}, owner={}", saved.getRoomId(), ownerUsername);
        return toDTO(saved);
    }

    public WatchPartyRoomDTO getRoom(String roomId) {
        WatchPartyRoom room = roomRepository.findByRoomIdAndActiveTrue(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));
        return toDTO(room);
    }

    public List<WatchPartyRoomDTO> getAllActiveRooms() {
        return roomRepository.findByActiveTrue()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void startVideo(String roomId, Long videoId, Long requesterId, String requesterUsername) {
        logger.info("START VIDEO called: roomId={}, videoId={}, requesterId={}", roomId, videoId, requesterId);

        WatchPartyRoom room = roomRepository.findByRoomIdAndActiveTrue(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));

        if (!room.getOwnerId().equals(requesterId)) {
            logger.error(" Permission denied: user {} is not owner of room {}", requesterId, roomId);
            throw new RuntimeException("Only the room owner can start a video");
        }

        room.setCurrentVideoId(videoId);
        roomRepository.save(room);
        logger.info("Video ID saved to DB: roomId={}, videoId={}", roomId, videoId);

        WatchPartyEventDTO event = WatchPartyEventDTO.redirectVideo(roomId, videoId, requesterUsername);

        logger.info("Publishing redirect_video to Redis: {}", event);
        redisPublisher.publish(event);

        logger.info(" Video start complete: roomId={}, videoId={}", roomId, videoId);
    }

    @Transactional
    public void closeRoom(String roomId, Long requesterId, String requesterUsername) {
        WatchPartyRoom room = roomRepository.findByRoomIdAndActiveTrue(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));

        if (!room.getOwnerId().equals(requesterId)) {
            throw new RuntimeException("Only the room owner can close the room");
        }

        room.setActive(false);
        roomRepository.save(room);

        WatchPartyEventDTO event = WatchPartyEventDTO.roomClosed(roomId, requesterUsername);
        redisPublisher.publish(event);

        logger.info("Room closed: roomId={}", roomId);
    }

    public void notifyUserJoined(String roomId, String username) {
        WatchPartyEventDTO event = WatchPartyEventDTO.userJoined(roomId, username);
        messagingTemplate.convertAndSend("/topic/watch-party/" + roomId, event);
        logger.info("User joined notification sent: room={}, user={}", roomId, username);
    }

    private WatchPartyRoomDTO toDTO(WatchPartyRoom room) {
        return new WatchPartyRoomDTO(
                room.getRoomId(),
                room.getOwnerId(),
                room.getOwnerUsername(),
                room.getCurrentVideoId(),
                room.isActive()
        );
    }
}