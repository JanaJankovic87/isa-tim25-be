package net.javaguides.springboot_jutjubic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/api/videos")
@CrossOrigin(origins = "http://localhost:4200")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private UserService userService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ObjectMapper objectMapper = new ObjectMapper();

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Korisnik nije autentifikovan");
        }

        String username = null;

        // UserDetails
        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            username = userDetails.getUsername();
        }
        // username string
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

    // POST - Upload videa
    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addVideo(
            @RequestParam("data") String dataJson,
            @RequestParam("thumbnail") MultipartFile thumbnail,
            @RequestParam("video") MultipartFile videoFile) {

        try {
            logger.info("Primljen zahtev za kreiranje video objave");

            // Parse DTO iz JSON-a
            VideoDTO dto = objectMapper.readValue(dataJson, VideoDTO.class);

            // Validacija DTO-a
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

            // DTO -> Entity konverzija sa trenutnim korisnikom
            Video video = new Video(dto.getTitle(), dto.getDescription(),
                    dto.getTags(), currentUser.getId()); //  Koristi ID trenutnog korisnika
            video.setLocation(dto.getLocation());

            logger.info("Pokušavam da sačuvam video: {}", video.getTitle());

            // Čuvanje sa transakcijom
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

    // PUT - Izmena videa
    @PutMapping(value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Video> updateVideo(@RequestBody Video video)
            throws ObjectOptimisticLockingFailureException {
        logger.info("Ažuriranje videa sa ID: {}", video.getId());
        Video updatedVideo = videoService.update(video);
        return new ResponseEntity<>(updatedVideo, HttpStatus.OK);
    }

    // DELETE - Brisanje videa
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long id) {
        logger.info("Brisanje videa sa ID: {}", id);
        videoService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // GET - Lista videa (JAVNO)
    @GetMapping(value = "/")
    public ResponseEntity<List<Video>> getVideos() {
        logger.info("Dobavljanje svih videa sortiranih po datumu");
        List<Video> videos = videoService.findAllSortedByDate();
        return new ResponseEntity<>(videos, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Video> getVideo(@PathVariable Long id) {
        logger.info("Dobavljanje videa sa ID: {}", id);
        Video video = videoService.findById(id);
        if (video == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(video, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}/thumbnail", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getThumbnail(@PathVariable Long id) {
        try {
            logger.info("Dobavljanje thumbnail-a za video ID: {}", id);
            byte[] thumbnail = videoService.getThumbnail(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(thumbnail);
        } catch (IOException e) {
            logger.error("Greška pri učitavanju thumbnail-a za ID: {}", id, e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @GetMapping(value = "/{id}/video")
    public ResponseEntity<byte[]> getVideoFile(@PathVariable Long id) {
        try {
            logger.info("Dobavljanje video stream-a za ID: {}", id);
            byte[] video = videoService.getVideoFile(id);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("video/mp4"))
                    .header("Content-Disposition", "inline; filename=\"video.mp4\"")
                    .body(video);

        } catch (IOException e) {
            logger.error("Greška pri učitavanju video stream-a za ID: {}", id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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
    public ResponseEntity<?> likeVideo(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            Video video = videoService.findById(id);

            if (video == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Video nije pronađen");
            }

            videoService.likeVideo(id, currentUser.getId());

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
    public ResponseEntity<?> recordView(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            Video video = videoService.findById(id);

            if (video == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Video nije pronađen");
            }

            videoService.recordView(id, currentUser.getId());

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
}
