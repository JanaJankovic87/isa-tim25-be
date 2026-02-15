package net.javaguides.springboot_jutjubic.service.impl;

import net.javaguides.springboot_jutjubic.dto.VideoPlaybackState;
import net.javaguides.springboot_jutjubic.model.Video;
import net.javaguides.springboot_jutjubic.repository.VideoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduledVideoService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledVideoService.class);

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Izračunava trenutnu sekundu videa na osnovu scheduled time-a
     */
    public Integer calculateCurrentSecond(Video video) {
        if (video.getScheduledTime() == null) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduledTime = video.getScheduledTime();

        // Ako video još nije počeo
        if (now.isBefore(scheduledTime)) {
            return -1; // Nije počeo
        }

        // Koliko sekundi je prošlo od zakazanog vremena
        long secondsSinceStart = ChronoUnit.SECONDS.between(scheduledTime, now);

        // Ako je video završen
        if (secondsSinceStart >= video.getVideoDurationSeconds()) {
            return video.getVideoDurationSeconds().intValue(); // Završen
        }

        return (int) secondsSinceStart;
    }

    /**
     * Vraća playback state za video
     */
    public VideoPlaybackState getPlaybackState(Long videoId) {
        Video video = videoRepository.findById(videoId).orElse(null);

        if (video == null) {
            return null;
        }

        Integer currentSecond = calculateCurrentSecond(video);

        return new VideoPlaybackState(
                video.getId(),
                video.getScheduledTime(),
                video.getVideoDurationSeconds(),
                currentSecond
        );
    }

    /**
     * Svake sekunde broadcast-uje trenutno stanje svih aktivnih scheduled videa
     */
    @Scheduled(fixedRate = 1000) // Svake sekunde
    public void broadcastActiveVideos() {
        LocalDateTime now = LocalDateTime.now();

        // Pronađi sve zakazane videe koji su aktivni (započeli su ali nisu završeni)
        List<Video> activeVideos = videoRepository.findAll().stream()
                .filter(v -> v.getIsScheduled() != null && v.getIsScheduled())
                .filter(v -> v.getScheduledTime() != null)
                .filter(v -> {
                    LocalDateTime scheduledTime = v.getScheduledTime();
                    long secondsSinceStart = ChronoUnit.SECONDS.between(scheduledTime, now);
                    return secondsSinceStart >= 0 && secondsSinceStart < v.getVideoDurationSeconds();
                })
                .collect(Collectors.toList());

        // Za svaki aktivan video, broadcast-uj trenutno stanje
        for (Video video : activeVideos) {
            VideoPlaybackState state = getPlaybackState(video.getId());

            // Pošalji na WebSocket topic za taj video
            messagingTemplate.convertAndSend(
                    "/topic/video/" + video.getId() + "/playback",
                    state
            );

            logger.debug("Broadcasting playback state for video {}: second {}",
                    video.getId(), state.getCurrentSecond());
        }
    }

    /**
     * Provera da li je video dostupan za gledanje
     */
    public boolean isVideoAvailable(Video video) {
        if (video.getIsScheduled() == null || !video.getIsScheduled()) {
            return true; // Nije zakazan, dostupan je odmah
        }

        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(video.getScheduledTime());
    }

    public Video.VideoStatus getVideoStatus(Video video) {
        // Ako video nije scheduled, vrati REGULAR
        if (video.getIsScheduled() == null || !video.getIsScheduled()) {
            return Video.VideoStatus.REGULAR;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduledTime = video.getScheduledTime();

        // Ako još nije počeo
        if (now.isBefore(scheduledTime)) {
            return Video.VideoStatus.SCHEDULED;
        }

        // Koliko sekundi je prošlo od početka
        long secondsSinceStart = ChronoUnit.SECONDS.between(scheduledTime, now);

        // Ako je završen
        if (secondsSinceStart >= video.getVideoDurationSeconds()) {
            return Video.VideoStatus.ENDED;
        }

        // Ako je trenutno LIVE
        return Video.VideoStatus.LIVE;
    }
}