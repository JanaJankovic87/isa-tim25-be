package net.javaguides.springboot_jutjubic.service.impl;

import net.javaguides.springboot_jutjubic.model.Video;
import net.javaguides.springboot_jutjubic.repository.VideoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

@Component
public class SeedTranscodingRunner {

    private static final Logger logger = LoggerFactory.getLogger(SeedTranscodingRunner.class);

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private TranscodingProducer transcodingProducer;

    @EventListener(ApplicationReadyEvent.class)
    public void transcodeExistingVideos() {
        List<Video> allVideos = videoRepository.findAll();
        int count = 0;

        for (Video video : allVideos) {
            if (video.getTranscodingStatus() == Video.TranscodingStatus.COMPLETED) {
                logger.info("Video ID={} already complited, skipping", video.getId());
                continue;
            }

            Path p720 = Paths.get("uploads/transcoded/" + video.getId() + "/720p.mp4").toAbsolutePath();
            Path p480 = Paths.get("uploads/transcoded/" + video.getId() + "/480p.mp4").toAbsolutePath();
            if (Files.exists(p720) && Files.exists(p480)) {
                video.setTranscodingStatus(Video.TranscodingStatus.COMPLETED);
                videoRepository.save(video);
                logger.info("Video ID={} files exist, marking as complited", video.getId());
                continue;
            }

            String pathToUse = (video.getOriginalVideoPath() != null && !video.getOriginalVideoPath().isEmpty())
                    ? video.getOriginalVideoPath()
                    : video.getVideoPath();

            if (pathToUse == null || pathToUse.isEmpty()) {
                logger.warn("Video ID={} has no path, skipping", video.getId());
                continue;
            }

            Path absolutePath = Paths.get(pathToUse.replace("\\", "/")).toAbsolutePath();
            if (!Files.exists(absolutePath)) {
                logger.warn("File does not exist: {} for video ID={}", absolutePath, video.getId());
                continue;
            }

            if (video.getTranscodingStatus() == Video.TranscodingStatus.FAILED
                    || video.getTranscodingStatus() == Video.TranscodingStatus.PROCESSING) {
                video.setTranscodingStatus(Video.TranscodingStatus.PENDING);
                videoRepository.save(video);
                logger.info("Video ID={} reset from {} to pending", video.getId(), video.getTranscodingStatus());
            }

            transcodingProducer.sendTranscodingRequest(video.getId(), absolutePath.toString());
            count++;
            logger.info("Transcoding request sent for video ID={}", video.getId());
        }

        logger.info("=== Transcoding started for {} videos ===", count);
    }
}
