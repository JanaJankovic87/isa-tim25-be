package net.javaguides.springboot_jutjubic.service.impl;

import jakarta.annotation.PostConstruct;
import net.javaguides.springboot_jutjubic.model.Video;
import net.javaguides.springboot_jutjubic.repository.VideoRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ThumbnailCompressionService {

    private static final Logger logger = LoggerFactory.getLogger(ThumbnailCompressionService.class);
    private static final String COMPRESSED_SUFFIX = "_compressed";

    @Value("${app.compression.quality:0.85}")
    private float compressionQuality;

    @Value("${app.compression.days-threshold:30}")
    private int daysThreshold;

    @Autowired
    private VideoRepository videoRepository;

    @PostConstruct
    public void runOnStartup() {
        logger.info(" Pokretanje inicijalne kompresije pri startu aplikacije ");
        compressOldThumbnails();
    }

    @Scheduled(cron = "${app.compression.cron}")
    public void scheduledCompression() {
        int count = compressOldThumbnails();
    }

    public int compressOldThumbnails() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysThreshold);
        List<Video> allVideos = videoRepository.findAll();

        int compressedCount = 0;
        int skippedCount = 0;
        int errorCount = 0;

        logger.info(" Pokretanje kompresije thumbnail slika ");
        logger.info("Prag starosti: {} dana (pre {})", daysThreshold, cutoff);
        logger.info("Kvalitet kompresije: {}", compressionQuality);
        logger.info("Ukupno video objava: {}", allVideos.size());

        for (Video video : allVideos) {

            if (video.getThumbnailPath() == null || video.getThumbnailPath().isEmpty()) {
                skippedCount++;
                continue;
            }

            if (video.getCreatedAt() == null || !video.getCreatedAt().isBefore(cutoff)) {
                skippedCount++;
                continue;
            }

            if (video.isThumbnailCompressed()) {
                skippedCount++;
                continue;
            }

            String fileName = video.getThumbnailPath().toLowerCase();
            if (fileName.endsWith(".gif")) {
                logger.info("Video ID={}: preskacem GIF thumbnail", video.getId());
                skippedCount++;
                continue;
            }

            try {
                boolean success = compressThumbnail(video);
                if (success) compressedCount++;
                else errorCount++;
            } catch (Exception e) {
                logger.error("Greska za video ID={}: {}", video.getId(), e.getMessage());
                errorCount++;
            }
        }

        logger.info("Kompresovano: {} , Preskoceno: {} | Greske: {}",
                compressedCount, skippedCount, errorCount);

        return compressedCount;
    }

    private boolean compressThumbnail(Video video) {
        Path originalPath = Paths.get(video.getThumbnailPath());



        if (!Files.exists(originalPath)) {
            logger.warn("Fajl ne postoji: {} (video ID={})", originalPath, video.getId());
            return false;
        }

        Path compressedPath = buildCompressedPath(originalPath);

        try {
            long originalSize = Files.size(originalPath);

            Thumbnails.of(originalPath.toFile())
                    .outputFormat("jpg")
                    .outputQuality(compressionQuality)
                    .size(1280, 720)
                    .keepAspectRatio(true)
                    .toFile(compressedPath.toFile());

            long compressedSize = Files.size(compressedPath);
            double reduction = 100.0 * (originalSize - compressedSize) / originalSize;

            if (compressedSize >= originalSize) {
                logger.info("Video ID={}: slika vec optimizovana, zadrzavam original", video.getId());
                deleteFileIfExists(compressedPath);
                return false;
            }

            logger.info("Video ID={}: {} → {} ({}% manji)",
                    video.getId(), formatSize(originalSize),
                    formatSize(compressedSize), String.format("%.1f", reduction));

            video.setCompressedThumbnailPath(compressedPath.toString());
            video.setThumbnailCompressed(true);
            videoRepository.save(video);

            return true;

        } catch (IOException e) {
            logger.error("Greska pri kompresiji video ID={}: {}", video.getId(), e.getMessage());
            deleteFileIfExists(compressedPath);
            return false;
        }
    }

    private Path buildCompressedPath(Path originalPath) {
        String fileName = originalPath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');

        String compressedFileName;
        if (dotIndex >= 0) {
            compressedFileName = fileName.substring(0, dotIndex)
                    + COMPRESSED_SUFFIX
                    + fileName.substring(dotIndex);
        } else {
            compressedFileName = fileName + COMPRESSED_SUFFIX;
        }

        return originalPath.getParent().resolve(compressedFileName);
    }

    private void deleteFileIfExists(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            logger.warn("Nije moguce obrisati fajl: {}", path);
        }
    }

    private String formatSize(long bytes) {
        if (bytes >= 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        if (bytes >= 1024) return String.format("%.1f KB", bytes / 1024.0);
        return bytes + " B";
    }
}