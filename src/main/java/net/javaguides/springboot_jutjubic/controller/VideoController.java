package net.javaguides.springboot_jutjubic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import net.javaguides.springboot_jutjubic.dto.LocationDTO;
import net.javaguides.springboot_jutjubic.dto.TrendingVideoDTO;
import net.javaguides.springboot_jutjubic.dto.VideoPlaybackState;
import net.javaguides.springboot_jutjubic.service.impl.GeolocationService;
import net.javaguides.springboot_jutjubic.service.impl.ThumbnailCompressionService;
import net.javaguides.springboot_jutjubic.service.impl.ScheduledVideoService;
import net.javaguides.springboot_jutjubic.service.impl.TranscodingProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import net.javaguides.springboot_jutjubic.dto.VideoDTO;
import net.javaguides.springboot_jutjubic.model.Video;
import net.javaguides.springboot_jutjubic.model.User;
import net.javaguides.springboot_jutjubic.service.VideoService;
import net.javaguides.springboot_jutjubic.service.UserService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.IOException;
import java.util.List;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

@RestController
@RequestMapping(value = "/api/videos")
@CrossOrigin(origins = "http://localhost:4200")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private UserService userService;

    @Autowired
    private GeolocationService geolocationService;

    @Autowired
    private TranscodingProducer transcodingProducer;

    @Autowired
    private ThumbnailCompressionService thumbnailCompressionService;
    
    @Autowired
    private ScheduledVideoService scheduledVideoService;

    @Value("${app.transcoding.output-dir:uploads/transcoded}")
    private String transcodedOutputDir;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Korisnik nije autentifikovan");
        }

        String username = null;

        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            username = userDetails.getUsername();
        }
        else if (authentication.getPrincipal() instanceof String) {
            username = (String) authentication.getPrincipal();
        }

        if (username == null) {
            throw new RuntimeException("Ne mogu da pronađem username");
        }

        User user = userService.findByUsername(username);

        if (user == null) {
            throw new RuntimeException("Korisnik nije pronađen: " + username);
        }

        return user;
    }

    private LocationDTO resolveLocation(Double lat, Double lng, HttpServletRequest request) {
        if (lat != null && lng != null) {
            LocationDTO location = new LocationDTO(lat, lng, false);
            location.setLocationName("GPS location");
            return location;
        }

        String ipAddress = extractClientIP(request);

        LocationDTO location = geolocationService.getLocationFromIP(ipAddress);

        if (location == null) {
            return null;
        }

        return location;
    }

    private String extractClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "127.0.0.1";
    }

    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addVideo(
            @RequestParam("data") String dataJson,
            @RequestParam("thumbnail") MultipartFile thumbnail,
            @RequestParam("video") MultipartFile videoFile) {

        try {
            logger.info("Primljen zahtev za kreiranje video objave");

            VideoDTO dto = objectMapper.readValue(dataJson, VideoDTO.class);

            if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
                logger.error("Validacija nije prošla: Title is required");
                return new ResponseEntity<>("Title is required", HttpStatus.BAD_REQUEST);
            }

            if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
                logger.error("Validacija nije prošla: Description is required");
                return new ResponseEntity<>("Description is required", HttpStatus.BAD_REQUEST);
            }

            if (dto.getTags() == null || dto.getTags().isEmpty()) {
                logger.error("Validacija nije prošla: At least one tag is required");
                return new ResponseEntity<>("At least one tag is required", HttpStatus.BAD_REQUEST);
            }


            User currentUser = getCurrentUser();
            logger.info("Korisnik {} kreira video", currentUser.getUsername());

            Video video = new Video(dto.getTitle(), dto.getDescription(),
                    dto.getTags(), currentUser.getId());
            video.setLocation(dto.getLocation());

            if (dto.getScheduledTime() != null) {
                video.setScheduledTime(dto.getScheduledTime());
                video.setIsScheduled(true);
            } else {
                video.setIsScheduled(false);
            }

            logger.info("Pokušavam da sačuvam video: {}", video.getTitle());

            Video savedVideo = videoService.save(video, thumbnail, videoFile);

            logger.info("Video uspešno sačuvan sa ID: {}", savedVideo.getId());
            return new ResponseEntity<>(savedVideo, HttpStatus.CREATED);

        } catch (RuntimeException re) {

            logger.error("Greška sa autentifikacijom", re);
            return new ResponseEntity<>(re.getMessage(), HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            logger.error("Greška pri kreiranju video objave", e);
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Video> updateVideo(@RequestBody Video video)
            throws ObjectOptimisticLockingFailureException {
        logger.info("Ažuriranje videa sa ID: {}", video.getId());
        Video updatedVideo = videoService.update(video);
        return new ResponseEntity<>(updatedVideo, HttpStatus.OK);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long id) {
        logger.info("Brisanje videa sa ID: {}", id);
        videoService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @GetMapping(value = "/")
    public ResponseEntity<?> getVideos() {
        logger.info("Dobavljanje svih videa sortiranih po datumu");
        List<Video> videos = videoService.findAllSortedByDate();

        List<Map<String, Object>> response = new ArrayList<>();

        for (Video video : videos) {
            Video.VideoStatus status = scheduledVideoService.getVideoStatus(video);

            Map<String, Object> videoData = new LinkedHashMap<>();
            videoData.put("id", video.getId());
            videoData.put("title", video.getTitle());
            videoData.put("description", video.getDescription());
            videoData.put("tags", video.getTags());
            videoData.put("thumbnailPath", video.getThumbnailPath());
            videoData.put("createdAt", video.getCreatedAt());
            videoData.put("location", video.getLocation());
            videoData.put("latitude", video.getLatitude());
            videoData.put("longitude", video.getLongitude());
            videoData.put("userId", video.getUserId());
            videoData.put("version", video.getVersion());

            videoData.put("isScheduled", video.getIsScheduled());
            videoData.put("scheduledTime", video.getScheduledTime());
            videoData.put("videoDurationSeconds", video.getVideoDurationSeconds());
            videoData.put("status", status.name());

            if (status == Video.VideoStatus.LIVE || status == Video.VideoStatus.ENDED) {
                Integer currentSecond = scheduledVideoService.calculateCurrentSecond(video);
                videoData.put("currentSecond", currentSecond);
            }

            if (status == Video.VideoStatus.SCHEDULED) {
                LocalDateTime now = LocalDateTime.now();
                long secondsUntilStart = ChronoUnit.SECONDS.between(now, video.getScheduledTime());
                videoData.put("secondsUntilStart", secondsUntilStart);
                videoData.put("message", "Počinje za " + formatDuration(secondsUntilStart));
            }

            videoData.put("videoPath", video.getVideoPath());

            response.add(videoData);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private String formatDuration(long seconds) {
        if (seconds < 60) {
            return seconds + " sekundi";
        } else if (seconds < 3600) {
            return (seconds / 60) + " minuta";
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return hours + "h " + minutes + "min";
        }
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<?> getVideo(@PathVariable Long id) {
        logger.info("Dobavljanje videa sa ID: {}", id);
        Video video = videoService.findById(id);
        if (video == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Video.VideoStatus status = scheduledVideoService.getVideoStatus(video);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", video.getId());
        response.put("title", video.getTitle());
        response.put("description", video.getDescription());
        response.put("tags", video.getTags());
        response.put("thumbnailPath", video.getThumbnailPath());
        response.put("createdAt", video.getCreatedAt());
        response.put("location", video.getLocation());
        response.put("latitude", video.getLatitude());
        response.put("longitude", video.getLongitude());
        response.put("userId", video.getUserId());
        response.put("version", video.getVersion());

        response.put("isScheduled", video.getIsScheduled());
        response.put("scheduledTime", video.getScheduledTime());
        response.put("videoDurationSeconds", video.getVideoDurationSeconds());
        response.put("status", status.name());

        if (status == Video.VideoStatus.LIVE || status == Video.VideoStatus.ENDED) {
            Integer currentSecond = scheduledVideoService.calculateCurrentSecond(video);
            response.put("currentSecond", currentSecond);
        }

        if (status == Video.VideoStatus.SCHEDULED) {
            response.put("message", "Video će biti dostupan: " + video.getScheduledTime());
        } else {
            response.put("videoPath", video.getVideoPath());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{id}/thumbnail", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getThumbnail(@PathVariable Long id) {
        try {

            Video video = videoService.findById(id);
            if (video != null && video.isThumbnailCompressed()) {
                logger.info("Video ID={}: serviram kompresovanu sliku", id);
            } else {
                logger.info("Video ID={}: serviram originalnu sliku", id);
            }

            logger.info("Dobavljanje thumbnail-a za video ID: {}", id);
            byte[] thumbnail = videoService.getThumbnail(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(thumbnail);
        } catch (IOException e) {
            logger.error("Greska pri učitavanju thumbnail-a za ID: {}", id, e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @GetMapping(value = "/{id}/video")
    public ResponseEntity<?> getVideoFile(
            @PathVariable Long id,
            @RequestHeader(value = "Range", required = false) String rangeHeader) {
        try {
            Video video = videoService.findById(id);
            if (video == null) {
                return ResponseEntity.notFound().build();
            }

            if (!scheduledVideoService.isVideoAvailable(video)) {
                Map<String, Object> errorResponse = new LinkedHashMap<>();
                errorResponse.put("error", "Video nije dostupan");
                errorResponse.put("scheduledTime", video.getScheduledTime());
                errorResponse.put("message", "Video je zakazan za: " + video.getScheduledTime());

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(errorResponse);
            }

            Path videoPath = Paths.get(video.getVideoPath());
            Resource resource = new FileSystemResource(videoPath);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("video/mp4"))
                    .header("Accept-Ranges", "bytes")
                    .header("Content-Disposition", "inline; filename=\"video.mp4\"")
                    .body(resource);

        } catch (Exception e) {
            logger.error("Greška pri učitavanju video stream-a za ID: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping(value = "/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Video service is running");
    }

    @GetMapping("/search")
    public ResponseEntity<List<Video>> searchVideos(@RequestParam("keyword") String keyword) {
        List<Video> videos = videoService.searchByKeyword(keyword);
        return ResponseEntity.ok(videos);
    }



    @PostMapping("/{id}/like")
    public ResponseEntity<?> likeVideo(@PathVariable Long id,
                                       @RequestBody(required = false) LocationDTO locationDTO,
                                       HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser();
            Video video = videoService.findById(id);

            if (video == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Video nije pronađen");
            }

            LocationDTO location;
            if (locationDTO != null && locationDTO.getLatitude() != null && locationDTO.getLongitude() != null) {
                location = locationDTO;
            } else {
                String ipAddress = extractClientIP(request);
                location = geolocationService.getLocationFromIP(ipAddress);
            }

            videoService.likeVideo(id, currentUser.getId(), location);

            long likesCount = videoService.getLikesCount(id);

            return ResponseEntity.ok()
                    .body("Video uspešno lajkovan. Ukupno lajkova: " + likesCount);
        } catch (RuntimeException e) {
            logger.error("Greška pri lajkovanju videa", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("You must be logged in to like videos");
        }
    }


    @DeleteMapping("/{id}/like")
    public ResponseEntity<?> unlikeVideo(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            Video video = videoService.findById(id);

            if (video == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Video nije pronađen");
            }

            videoService.unlikeVideo(id, currentUser.getId());

            long likesCount = videoService.getLikesCount(id);

            return ResponseEntity.ok()
                    .body("Lajk uklonjen. Ukupno lajkova: " + likesCount);
        } catch (RuntimeException e) {
            logger.error("Greška pri uklanjanju lajka", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("You must be logged in to unlike videos");
        }
    }

    @GetMapping("/{id}/likes/count")
    public ResponseEntity<Long> getLikesCount(@PathVariable Long id) {
        Video video = videoService.findById(id);

        if (video == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        long likesCount = videoService.getLikesCount(id);
        return ResponseEntity.ok(likesCount);
    }

    @GetMapping("/{id}/likes/status")
    public ResponseEntity<?> isLikedByCurrentUser(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            Video video = videoService.findById(id);

            if (video == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Video nije pronađen");
            }

            boolean isLiked = videoService.isVideoLikedByUser(id, currentUser.getId());
            return ResponseEntity.ok(isLiked);
        } catch (RuntimeException e) {
            return ResponseEntity.ok(false);
        }
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<?> recordView(@PathVariable Long id, @RequestBody(required = false) LocationDTO locationDTO,
                                        HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser();
            Video video = videoService.findById(id);

            if (video == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Video nije pronađen");
            }

            LocationDTO location;
            if (locationDTO != null && locationDTO.getLatitude() != null && locationDTO.getLongitude() != null) {
                location = locationDTO;
            } else {
                String ipAddress = extractClientIP(request);
                location = geolocationService.getLocationFromIP(ipAddress);
            }

            videoService.recordView(id, currentUser.getId(), location);

            long viewCount = videoService.getViewCount(id);

            logger.info("View registrovan: korisnik {} pogledao video {}. Ukupno: {}",
                    currentUser.getId(), id, viewCount);

            return ResponseEntity.ok()
                    .body("View registrovan. Ukupno pregleda: " + viewCount);

        } catch (RuntimeException e) {
            logger.error("Greška pri registrovanju view-a", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("You must be logged in to record views");
        }
    }



    @GetMapping("/{id}/views/count")
    public ResponseEntity<Long> getViewCount(@PathVariable Long id) {
        Video video = videoService.findById(id);

        if (video == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        long viewCount = videoService.getViewCount(id);
        logger.info("View count za video {}: {}", id, viewCount);

        return ResponseEntity.ok(viewCount);
    }

    @GetMapping("/trending")
    public ResponseEntity<List<TrendingVideoDTO>> getTrendingVideos() {
        return ResponseEntity.ok(videoService.getTrendingVideos());
    }

    @GetMapping(value = "/{id}/video/{preset}")
    public ResponseEntity<Resource> getVideoFileByPreset(
            @PathVariable Long id,
            @PathVariable String preset) {
        try {
            if (!preset.equals("720p") && !preset.equals("480p")) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }


            String transcodedPath = transcodedOutputDir + "/" + id + "/" + preset + ".mp4";
            Path videoPath = Paths.get(transcodedPath);

            if (Files.exists(videoPath)) {
                Resource resource = new FileSystemResource(videoPath);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("video/mp4"))
                        .header("Accept-Ranges", "bytes")
                        .header("Content-Disposition",
                                "inline; filename=\"video_" + preset + ".mp4\"")
                        .body(resource);
            }


            Video video = videoService.findById(id);
            if (video == null || video.getVideoPath() == null) {
                logger.warn("Video ID={} nije pronađen u bazi, ni transcoded fajl za {}", id, preset);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            Path originalPath = Paths.get(video.getVideoPath());
            if (!Files.exists(originalPath)) {
                logger.warn("Video ID={} original fajl ne postoji: {}", id, originalPath);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            Resource resource = new FileSystemResource(originalPath);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("video/mp4"))
                    .header("Accept-Ranges", "bytes")
                    .header("X-Transcoded", "false")
                    .body(resource);

        } catch (Exception e) {
            logger.error("Greška pri učitavanju video stream-a za ID={}, preset={}", id, preset, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/admin/sync-transcoded")
    public ResponseEntity<Map<String, Object>> syncTranscodedFiles() {
        Map<String, Object> result = new LinkedHashMap<>();
        List<String> synced = new ArrayList<>();
        List<String> skipped = new ArrayList<>();

        List<Video> allVideos = videoService.findAll();

        for (Video video : allVideos) {
            Path p720 = Paths.get(transcodedOutputDir + "/" + video.getId() + "/720p.mp4");
            Path p480 = Paths.get(transcodedOutputDir + "/" + video.getId() + "/480p.mp4");

            boolean filesExist = Files.exists(p720) && Files.exists(p480);

            if (filesExist && video.getTranscodingStatus() != Video.TranscodingStatus.COMPLETED) {
                video.setTranscodingStatus(Video.TranscodingStatus.COMPLETED);
                video.setTranscodedDir(transcodedOutputDir + "/" + video.getId());
                videoService.update(video);
                synced.add("ID=" + video.getId() + " → COMPLETED");
            } else if (!filesExist) {
                skipped.add("ID=" + video.getId() + " → fajlovi ne postoje");
            }
        }

        result.put("synced", synced);
        result.put("skipped", skipped);
        logger.info("Sync završen: {}", result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/presets")
    public ResponseEntity<Map<String, Object>> getAvailablePresets(@PathVariable Long id) {
        logger.info("🔍 Checking available presets for video ID: {}", id);

        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Boolean> presets = new LinkedHashMap<>();

        String[] qualities = {"720p", "480p"};
        boolean allExist = true;

        for (String quality : qualities) {
            java.nio.file.Path path = Paths.get(transcodedOutputDir + "/" + id + "/" + quality + ".mp4");
            boolean exists = Files.exists(path);
            presets.put(quality, exists);

            if (!exists) {
                allExist = false;
            }

            logger.info("   - {} : {} (path: {})", quality, exists, path);
        }


        Video video = videoService.findById(id);
        String status = "PENDING";

        if (video != null && video.getTranscodingStatus() != null) {
            status = video.getTranscodingStatus().name();
        }

        logger.info("   - Transcoding status: {}", status);
        logger.info("   - All files exist: {}", allExist);

        response.put("presets", presets);
        response.put("transcodingStatus", status);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/transcoding-status")
    public ResponseEntity<Map<String, String>> getTranscodingStatus(@PathVariable Long id) {
        Video video = videoService.findById(id);
        if (video == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, String> response = new LinkedHashMap<>();
        response.put("status", video.getTranscodingStatus() != null
                ? video.getTranscodingStatus().name()
                : "PENDING");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/test/compress-thumbnails")
    public ResponseEntity<?> testCompression() {
        int count = thumbnailCompressionService.compressOldThumbnails();
        return ResponseEntity.ok("Kompresovano: " + count + " slika");
    }

    @GetMapping("/{id}/playback-state")
    public ResponseEntity<?> getPlaybackState(@PathVariable Long id) {
        try {
            Video video = videoService.findById(id);

            if (video == null) {
                return ResponseEntity.notFound().build();
            }

            if (!scheduledVideoService.isVideoAvailable(video)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Video nije dostupan. Zakazan je za: " + video.getScheduledTime());
            }

            VideoPlaybackState state = scheduledVideoService.getPlaybackState(id);
            return ResponseEntity.ok(state);

        } catch (Exception e) {
            logger.error("Greška pri dobavljanju playback state-a", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<?> checkAvailability(@PathVariable Long id) {
        Video video = videoService.findById(id);

        if (video == null) {
            return ResponseEntity.notFound().build();
        }

        boolean isAvailable = scheduledVideoService.isVideoAvailable(video);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("isAvailable", isAvailable);
        response.put("isScheduled", video.getIsScheduled());
        response.put("scheduledTime", video.getScheduledTime());

        if (!isAvailable) {
            response.put("message", "Video će biti dostupan " + video.getScheduledTime());
        }

        return ResponseEntity.ok(response);
    }
}
