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
        logger.info(" SEED TRANSCODING RUNNER - STARTED");

        List<Video> allVideos = videoRepository.findAll();
        int alreadyCompleted = 0;
        int statusUpdated = 0;
        int sent = 0;
        int skipped = 0;

        logger.info(" Found {} videos in database", allVideos.size());

        for (Video video : allVideos) {
            logger.info("\nProcessing Video ID: {} ---", video.getId());
            logger.info("   Title: {}", video.getTitle());
            logger.info("   Current status: {}", video.getTranscodingStatus());

            Path p720 = Paths.get("uploads/transcoded/" + video.getId() + "/720p.mp4").toAbsolutePath();
            Path p480 = Paths.get("uploads/transcoded/" + video.getId() + "/480p.mp4").toAbsolutePath();

            boolean exists720 = Files.exists(p720);
            boolean exists480 = Files.exists(p480);
            boolean allFilesExist = exists720 && exists480;

            logger.info("   720p exists: {} ({})", exists720, p720);
            logger.info("   480p exists: {} ({})", exists480, p480);

            if (allFilesExist && video.getTranscodingStatus() == Video.TranscodingStatus.COMPLETED) {
                logger.info("    Already COMPLETED and all files exist - SKIP");
                alreadyCompleted++;
                continue;
            }

            if (allFilesExist && video.getTranscodingStatus() != Video.TranscodingStatus.COMPLETED) {
                logger.info("    All files exist but status is {}", video.getTranscodingStatus());
                logger.info("    Updating status to COMPLETED...");

                video.setTranscodingStatus(Video.TranscodingStatus.COMPLETED);
                video.setTranscodedDir("uploads/transcoded/" + video.getId());
                videoRepository.save(video);

                logger.info("  Status updated to COMPLETED");
                statusUpdated++;
                continue;
            }

            String pathToUse = (video.getOriginalVideoPath() != null && !video.getOriginalVideoPath().isEmpty())
                    ? video.getOriginalVideoPath()
                    : video.getVideoPath();

            if (pathToUse == null || pathToUse.isEmpty()) {
                logger.warn("    No video path found - SKIP");
                skipped++;
                continue;
            }

            Path absolutePath = Paths.get(pathToUse.replace("\\", "/")).toAbsolutePath();
            if (!Files.exists(absolutePath)) {
                logger.warn("   ️ Source file does not exist: {} - SKIP", absolutePath);
                skipped++;
                continue;
            }

            logger.info("    Sending to transcoding queue...");
            logger.info("   Source: {}", absolutePath);

            if (video.getTranscodingStatus() == Video.TranscodingStatus.FAILED ||
                    video.getTranscodingStatus() == Video.TranscodingStatus.PROCESSING) {
                logger.info("  Resetting status from {} to PENDING", video.getTranscodingStatus());
                video.setTranscodingStatus(Video.TranscodingStatus.PENDING);
                videoRepository.save(video);
            }

            transcodingProducer.sendTranscodingRequest(video.getId(), absolutePath.toString());
            logger.info("   Transcoding request SENT");
            sent++;
        }


        logger.info(" SEED TRANSCODING RUNNER - SUMMARY");
        logger.info(" Already completed: {}", alreadyCompleted);
        logger.info(" Status updated: {}", statusUpdated);
        logger.info(" Sent to queue: {}", sent);
        logger.info("  Skipped: {}", skipped);
        logger.info(" Total videos: {}", allVideos.size());

    }
}
