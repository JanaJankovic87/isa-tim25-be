package net.javaguides.springboot_jutjubic.service.impl;

import net.javaguides.springboot_jutjubic.dto.VideoPlaybackState;
import net.javaguides.springboot_jutjubic.model.Video;
import net.javaguides.springboot_jutjubic.repository.VideoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ScheduledVideoService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledVideoService.class);
    private static final String REDIS_KEY_PREFIX = "scheduled:video:";
    private static final String CURRENT_SECOND_SUFFIX = ":currentSecond";

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Izračunava trenutnu sekundu videa na osnovu scheduled time-a
    // Koristi Redis cache za performanse
    public Integer calculateCurrentSecond(Video video) {
        if (video.getScheduledTime() == null) {
            return 0;
        }

        // pokušaj dobiti iz Redis cache-a
        String cacheKey = REDIS_KEY_PREFIX + video.getId() + CURRENT_SECOND_SUFFIX;
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                logger.debug("Redis cache hit za video {}: {} sekundi", video.getId(), cached);
                return Integer.parseInt(cached.toString());
            }
        } catch (Exception e) {
            logger.warn("Greška pri čitanju iz Redis cache-a za video {}", video.getId(), e);
        }

        // ako nema u cache-u, izračunaj
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduledTime = video.getScheduledTime();

        // ako video još nije počeo
        if (now.isBefore(scheduledTime)) {
            return -1; // Nije počeo
        }

        // koliko sekundi je prošlo od zakazanog vremena
        long secondsSinceStart = ChronoUnit.SECONDS.between(scheduledTime, now);

        // ako je video završen
        if (secondsSinceStart >= video.getVideoDurationSeconds()) {
            return video.getVideoDurationSeconds().intValue(); // Završen
        }

        int currentSecond = (int) secondsSinceStart;

        // keširaj u Redis sa TTL od 2 sekunde
        try {
            redisTemplate.opsForValue().set(cacheKey, currentSecond, 2, TimeUnit.SECONDS);
            logger.debug("Keširan currentSecond za video {}: {}", video.getId(), currentSecond);
        } catch (Exception e) {
            logger.warn("Greška pri keširanju u Redis za video {}", video.getId(), e);
        }

        return currentSecond;
    }

    // Dobavlja trenutno stanje reprodukcije videa
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

    // svake sekunde proverava koji su videi trenutno aktivni (LIVE) i broadcast-uje njihovo stanje na WebSocket topic
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

        // Za svaki aktivan video, ažuriraj cache i broadcast-uj trenutno stanje
        for (Video video : activeVideos) {
            // Ažuriraj Redis cache
            updateRedisCache(video);

            // Dobavi playback state (iz cache-a)
            VideoPlaybackState state = getPlaybackState(video.getId());

            // Pošalji na WebSocket topic za taj video
            messagingTemplate.convertAndSend(
                    "/topic/video/" + video.getId() + "/playback",
                    state
            );

            logger.debug("Broadcasting playback state for video {}: second {}",
                    video.getId(), state.getCurrentSecond());
        }

        // Očisti cache za završene videe
        cleanupFinishedVideos();
    }

    // Ažurira Redis cache sa trenutnom sekundom za dati video
    private void updateRedisCache(Video video) {
        LocalDateTime now = LocalDateTime.now();
        long secondsSinceStart = ChronoUnit.SECONDS.between(video.getScheduledTime(), now);
        int currentSecond = (int) Math.min(secondsSinceStart, video.getVideoDurationSeconds());

        String cacheKey = REDIS_KEY_PREFIX + video.getId() + CURRENT_SECOND_SUFFIX;

        try {
            redisTemplate.opsForValue().set(cacheKey, currentSecond, 2, TimeUnit.SECONDS);
            logger.debug("Redis cache ažuriran za video {}: {}", video.getId(), currentSecond);
        } catch (Exception e) {
            logger.error("Greška pri ažuriranju Redis cache-a za video {}", video.getId(), e);
        }
    }

    // Briše Redis cache za videe koji su završeni (prošlo je više sekundi od scheduled time-a nego što video traje)
    private void cleanupFinishedVideos() {
        try {
            // Pronađi sve završene zakazane videe
            LocalDateTime now = LocalDateTime.now();
            List<Video> finishedVideos = videoRepository.findAll().stream()
                    .filter(v -> v.getIsScheduled() != null && v.getIsScheduled())
                    .filter(v -> v.getScheduledTime() != null)
                    .filter(v -> {
                        long secondsSinceStart = ChronoUnit.SECONDS.between(v.getScheduledTime(), now);
                        return secondsSinceStart >= v.getVideoDurationSeconds();
                    })
                    .collect(Collectors.toList());

            // Obriši njihove cache keys
            for (Video video : finishedVideos) {
                String cacheKey = REDIS_KEY_PREFIX + video.getId() + CURRENT_SECOND_SUFFIX;
                redisTemplate.delete(cacheKey);
                logger.debug("Obrisan Redis cache za završeni video {}", video.getId());
            }
        } catch (Exception e) {
            logger.error("Greška pri čišćenju Redis cache-a", e);
        }
    }

    // proverava da li je video trenutno dostupan za gledanje (ako je zakazan, proverava da li je došlo vreme za početak)
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