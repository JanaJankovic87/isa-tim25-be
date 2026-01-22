package net.javaguides.springboot_jutjubic.service.impl;

import net.javaguides.springboot_jutjubic.dto.TrendingVideoDTO;
import net.javaguides.springboot_jutjubic.model.VideoLike;
import net.javaguides.springboot_jutjubic.service.VideoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import net.javaguides.springboot_jutjubic.model.Video;
import net.javaguides.springboot_jutjubic.model.VideoView;
import net.javaguides.springboot_jutjubic.repository.VideoRepository;
import net.javaguides.springboot_jutjubic.repository.VideoLikeRepository;
import net.javaguides.springboot_jutjubic.repository.VideoViewRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

@Service
public class VideoServiceImpl implements VideoService {

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private VideoLikeRepository videoLikeRepository;

    @Autowired
    private VideoViewRepository videoViewRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final long MAX_VIDEO_SIZE = 200 * 1024 * 1024; // 200MB
    private static final long UPLOAD_TIMEOUT_MS = 30000; // 30 sekundi

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Video save(Video video, MultipartFile thumbnail, MultipartFile videoFile)
            throws Exception {


        Path tempThumbnailPath = null;
        Path tempVideoPath = null;
        Path finalThumbnailPath = null;
        Path finalVideoPath = null;

        try {
            logger.info("Početak kreiranja video objave: {}", video.getTitle());

            // 1. Validacija fajlova
            validateFiles(thumbnail, videoFile);

            // 2. Kreiranje temp direktorijuma
            Path tempDir = Paths.get(uploadDir, "temp");
            Files.createDirectories(tempDir);

            // 3. Upload u temp sa timeout-om
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Path[]> uploadFuture = executor.submit(() -> {
                Path tempThumb = tempDir.resolve("thumb_" + System.currentTimeMillis() + ".jpg");
                Path tempVid = tempDir.resolve("video_" + System.currentTimeMillis() + ".mp4");

                Files.copy(thumbnail.getInputStream(), tempThumb, StandardCopyOption.REPLACE_EXISTING);
                Files.copy(videoFile.getInputStream(), tempVid, StandardCopyOption.REPLACE_EXISTING);

                return new Path[]{tempThumb, tempVid};
            });

            Path[] uploadedPaths;
            try {
                uploadedPaths = uploadFuture.get(UPLOAD_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                tempThumbnailPath = uploadedPaths[0];
                tempVideoPath = uploadedPaths[1];
            } catch (TimeoutException e) {
                uploadFuture.cancel(true);
                throw new Exception("Upload timeout - operacija je predugo trajala");
            } finally {
                executor.shutdown();
            }

            logger.info("Fajlovi uspešno uploadovani u temp folder");

            // 4. Čuvanje u bazi (dobijanje ID-a)
            Video savedVideo = videoRepository.save(video);
            logger.info("Video sačuvan u bazi sa ID: {}", savedVideo.getId());

            // 5. Premeštanje u finalne direktorijume
            Path thumbnailsDir = Paths.get(uploadDir, "thumbnails");
            Path videosDir = Paths.get(uploadDir, "videos");
            Files.createDirectories(thumbnailsDir);
            Files.createDirectories(videosDir);

            finalThumbnailPath = thumbnailsDir.resolve(savedVideo.getId() + ".jpg");
            finalVideoPath = videosDir.resolve(savedVideo.getId() + ".mp4");

            Files.move(tempThumbnailPath, finalThumbnailPath, StandardCopyOption.REPLACE_EXISTING);
            Files.move(tempVideoPath, finalVideoPath, StandardCopyOption.REPLACE_EXISTING);

            logger.info("Fajlovi premešteni u finalne direktorijume");

            // 6. Update putanja u bazi
            savedVideo.setThumbnailPath(finalThumbnailPath.toString());
            savedVideo.setVideoPath(finalVideoPath.toString());
            savedVideo = videoRepository.save(savedVideo);

            logger.info("Video objava uspešno kreirana: {}", savedVideo);
            return savedVideo;

        } catch (Exception e) {
            logger.error("Greška pri kreiranju objave, rollback...", e);

            // Cleanup svih fajlova
            deleteFileIfExists(tempThumbnailPath);
            deleteFileIfExists(tempVideoPath);
            deleteFileIfExists(finalThumbnailPath);
            deleteFileIfExists(finalVideoPath);

            throw e;
        }

    }

    @Override
    @Transactional(rollbackFor = ObjectOptimisticLockingFailureException.class)
    public Video update(Video video) throws ObjectOptimisticLockingFailureException {
        logger.info("Ažuriranje video objave: {}", video.getId());
        Video updated = videoRepository.save(video);
        invalidateThumbnailCache(updated.getId());
        return updated;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        logger.info("Brisanje video objave: {}", id);
        Video video = videoRepository.findById(id).orElse(null);

        if (video != null) {
            deleteFileIfExists(Paths.get(video.getThumbnailPath()));
            deleteFileIfExists(Paths.get(video.getVideoPath()));
            videoRepository.deleteById(id);
            invalidateThumbnailCache(id);
        }
    }

    @Override
    public List<Video> findAll() {
        return videoRepository.findAll();
    }

    @Override
    public Video findById(Long id) {
        return videoRepository.findById(id).orElse(null);
    }

    @Override
    @Cacheable(value = "thumbnails", key = "#id")
    public byte[] getThumbnail(Long id) throws IOException {
        logger.info("Učitavanje thumbnail-a sa disk-a za post: {}", id);
        Video video = videoRepository.findById(id).orElse(null);

        if (video == null || video.getThumbnailPath() == null) {
            throw new IOException("Thumbnail nije pronađen");
        }

        Path thumbnailPath = Paths.get(video.getThumbnailPath());
        return Files.readAllBytes(thumbnailPath);
    }

    @Override
    @CacheEvict(value = "thumbnails", key = "#id")
    public void invalidateThumbnailCache(Long id) {
        logger.info("Invalidacija thumbnail keša za post: {}", id);
    }

    private void validateFiles(MultipartFile thumbnail, MultipartFile video) throws Exception {
        if (thumbnail == null || thumbnail.isEmpty()) {
            throw new Exception("Thumbnail je obavezan");
        }

        if (video == null || video.isEmpty()) {
            throw new Exception("Video je obavezan");
        }

        if (!thumbnail.getContentType().startsWith("image/")) {
            throw new Exception("Thumbnail mora biti slika");
        }

        if (!"video/mp4".equals(video.getContentType())) {
            throw new Exception("Video mora biti MP4 format");
        }

        if (video.getSize() > MAX_VIDEO_SIZE) {
            throw new Exception("Video ne sme biti veći od 200MB");
        }
    }

    private void deleteFileIfExists(Path path) {
        if (path != null) {
            try {
                Files.deleteIfExists(path);
                logger.info("Fajl obrisan: {}", path);
            } catch (IOException e) {
                logger.error("Greška pri brisanju fajla: {}", path, e);
            }
        }
    }

    @Override
    public byte[] getVideoFile(Long id) throws IOException {
        logger.info("Učitavanje video fajla za ID: {}", id);

        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new IOException("Video sa ID " + id + " nije pronađen"));

        if (video.getVideoPath() == null || video.getVideoPath().isEmpty()) {
            throw new IOException("Video path nije postavljen za video ID: " + id);
        }

        Path videoPath = Paths.get(video.getVideoPath());

        if (!Files.exists(videoPath)) {
            logger.error("Video fajl ne postoji na putanji: {}", videoPath);
            throw new IOException("Video fajl ne postoji: " + videoPath);
        }

        logger.info("Video fajl pronađen: {}, veličina: {} bytes",
                videoPath, Files.size(videoPath));

        return Files.readAllBytes(videoPath);
    }

    public List<Video> searchByKeyword(String keyword) {
        List<Video> byTitle = videoRepository.findByTitleContainingIgnoreCase(keyword);
        return new ArrayList<>(byTitle);
    }

    @Override
    @Transactional
    public void likeVideo(Long videoId, Long userId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video sa ID " + videoId + " nije pronađen"));

        if (videoLikeRepository.existsByUserIdAndVideoId(userId, videoId)) {
            return;
        }

        VideoLike like = new VideoLike(userId, videoId);
        videoLikeRepository.save(like);
    }

    @Override
    @Transactional
    public void unlikeVideo(Long videoId, Long userId) {
        if (!videoLikeRepository.existsByUserIdAndVideoId(userId, videoId)) {
            logger.warn("Korisnik {} nije lajkovao video {} - ne može unlajkovati", userId, videoId);
            return;
        }

        videoLikeRepository.deleteByUserIdAndVideoId(userId, videoId);
        logger.info("Korisnik {} uklonio like sa videa {}", userId, videoId);
    }

    @Override
    public boolean isVideoLikedByUser(Long videoId, Long userId) {
        return videoLikeRepository.existsByUserIdAndVideoId(userId, videoId);
    }

    @Override
    public long getLikesCount(Long videoId) {
        return videoLikeRepository.countByVideoId(videoId);
    }

    @Override
    public List<Video> findByUserId(Long userId) {
        return videoRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void recordView(Long videoId, Long userId) {
        try {
            // 1. Pronađi video
            Video video = videoRepository.findById(videoId)
                    .orElseThrow(() -> new RuntimeException("Video sa ID " + videoId + " nije pronađen"));

            // 2. Proveri da li je korisnik AUTOR videa
            if (video.getUserId().equals(userId)) {
                logger.info("Korisnik {} je AUTOR videa {} - view se NE registruje", userId, videoId);
                return;
            }

            // 3. Proveri da li je korisnik VEĆ GLEDAO ovaj video
            if (videoViewRepository.existsByUserIdAndVideoId(userId, videoId)) {
                logger.info("Korisnik {} je VEĆ gledao video {} - view se NE registruje", userId, videoId);
                return;
            }

            // 4. Sačuvaj zapis da je korisnik pogledao video
            VideoView view = new VideoView(userId, videoId);
            videoViewRepository.save(view);

            logger.info("Registrovan view: korisnik {} pogledao video {}", userId, videoId);

        } catch (Exception e) {
            logger.error("Greška pri registrovanju view-a za video {}", videoId, e);

        }
    }

    @Override
    public long getViewCount(Long videoId) {
        try {
            long count = videoViewRepository.countByVideoId(videoId);
            logger.info("Video {} ima {} pregleda", videoId, count);
            return count;
        } catch (Exception e) {
            logger.error("Greška pri brojanju view-ova za video {}", videoId, e);
            return 0L;
        }
    }
    @Override
    public List<Video> findAllSortedByDate() {
        return videoRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public double calculateTrendingScore(Video video) {

        long views = getViewCount(video.getId());
        long likes = getLikesCount(video.getId());
        int comments = video.getComments().size();

        long hoursSinceUpload = ChronoUnit.HOURS
                .between(video.getCreatedAt(), LocalDateTime.now());

        double freshness = 1.0 / (1 + hoursSinceUpload);

        return views * 0.4
                + likes * 0.3
                + comments * 0.2
                + freshness * 0.1;
    }

    @Override
    public List<TrendingVideoDTO> getTrendingVideos() {

        List<Video> videos = findAll();
        List<TrendingVideoDTO> trendingList = new ArrayList<>();

        for (Video video : videos) {
            double score = calculateTrendingScore(video);
            trendingList.add(new TrendingVideoDTO(video, score));
        }

        Collections.sort(trendingList, new Comparator<TrendingVideoDTO>() {
            @Override
            public int compare(TrendingVideoDTO a, TrendingVideoDTO b) {
                return Double.compare(b.getTrendingScore(), a.getTrendingScore());
            }
        });

        List<TrendingVideoDTO> result = new ArrayList<>();
        for (int i = 0; i < trendingList.size() && i < 5; i++) {
            result.add(trendingList.get(i));
        }

        return result;
    }

}


