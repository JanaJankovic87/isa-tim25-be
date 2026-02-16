package net.javaguides.springboot_jutjubic.service.impl;

import com.rabbitmq.client.Channel;
import net.javaguides.springboot_jutjubic.config.TranscodingConfig;
import net.javaguides.springboot_jutjubic.dto.TranscodingMessage;
import net.javaguides.springboot_jutjubic.model.Video;
import net.javaguides.springboot_jutjubic.repository.VideoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;
import ws.schild.jave.info.VideoSize;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Service
public class TranscodingConsumer {

    private static final Logger logger = LoggerFactory.getLogger(TranscodingConsumer.class);

    @Autowired
    private VideoRepository videoRepository;

    private static final Map<String, VideoConfig> PRESET_CONFIGS = Map.of(
            "720p", new VideoConfig(1280, 720, 6000000, 128000),
            "480p", new VideoConfig(854,  480, 600000, 96000)
    );

    @RabbitListener(
            queues = TranscodingConfig.TRANSCODING_QUEUE,
            containerFactory = "rabbitListenerContainerFactory",
            concurrency = "2"
    )
    public void handleTranscodingRequest(
            TranscodingMessage message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {

        logger.info("[Consumer-{}] Received transcoding job for videoId={}",
                Thread.currentThread().getName(), message.getVideoId());

        try {
            updateTranscodingStatus(message.getVideoId(), Video.TranscodingStatus.PROCESSING);
            String outputDir = message.getOutputDir();
            String[] presets = message.getPresets();
            if (presets == null || presets.length == 0) {
                presets = new String[]{"720p", "480p"};
            }

            boolean alreadyDone = true;
            for (String preset : presets) {
                Path presetPath = Paths.get(outputDir + "/" + preset + ".mp4");
                if (!Files.exists(presetPath)) {
                    alreadyDone = false;
                    break;
                }
            }

            if (alreadyDone) {
                logger.info("[Consumer] Video ID={} već transkodovan, preskačem duplikat",
                        message.getVideoId());
                channel.basicAck(deliveryTag, false);
                return;
            }

            Files.createDirectories(Paths.get(outputDir));

            boolean allSuccess = true;

            for (String preset : presets) {
                boolean success = transcodeToPreset(message, preset);
                if (!success) {
                    allSuccess = false;
                    logger.warn("[Consumer] Failed preset={} for videoId={}", preset, message.getVideoId());
                }
            }

            if (allSuccess) {
                updateVideoTranscodedPath(message.getVideoId(), outputDir, "720p");

                updateTranscodingStatus(message.getVideoId(), Video.TranscodingStatus.COMPLETED);
                logger.info("[Consumer] Transcoding COMPLETE for videoId={}", message.getVideoId());
            } else {

                updateTranscodingStatus(message.getVideoId(), Video.TranscodingStatus.FAILED);
            }

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            logger.error("[Consumer] Transcoding FAILED for videoId={}: {}",
                    message.getVideoId(), e.getMessage(), e);

            updateTranscodingStatus(message.getVideoId(), Video.TranscodingStatus.FAILED);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ioEx) {
                logger.error("Failed to NACK message", ioEx);
            }
        }
    }

    private boolean transcodeToPreset(TranscodingMessage message, String preset) {
        VideoConfig config = PRESET_CONFIGS.getOrDefault(preset,
                new VideoConfig(1280, 720, 2500000, 128000));

        String outputFile = message.getOutputDir() + "/" + preset + ".mp4";

        logger.info("[Consumer] Transcoding videoId={} to preset={}, output={}",
                message.getVideoId(), preset, outputFile);

        try {
            File source = Paths.get(message.getVideoPath()).toAbsolutePath().toFile();
            File target = Paths.get(outputFile).toAbsolutePath().toFile();

            target.getParentFile().mkdirs();

            if (!source.exists()) {
                logger.error("Consumer Source file does NOT exist: {}", source.getAbsolutePath());
                return false;
            }

            logger.info("Consumer Source absolute path: {}", source.getAbsolutePath());
            logger.info("Consumer Target absolute path: {}", target.getAbsolutePath());

            AudioAttributes audio = new AudioAttributes();
            audio.setCodec("aac");
            audio.setBitRate(config.audioBitrate);
            audio.setChannels(2);
            audio.setSamplingRate(44100);

            VideoAttributes video = new VideoAttributes();
            video.setCodec("h264");
            video.setBitRate(config.videoBitrate);
            video.setSize(new VideoSize(config.width, config.height));
            video.setFrameRate(30);
            video.setPixelFormat("yuv420p");

            EncodingAttributes attrs = new EncodingAttributes();
            attrs.setOutputFormat("mp4");
            attrs.setAudioAttributes(audio);
            attrs.setVideoAttributes(video);

            Encoder encoder = new Encoder();
            encoder.encode(new MultimediaObject(source), target, attrs);

            logger.info("Consumer Preset={} done for videoId={}", preset, message.getVideoId());
            return true;

        } catch (Exception e) {
            logger.error("Consumer Error transcoding preset={}: {}", preset, e.getMessage(), e);
            return false;
        }
    }

    private void updateVideoTranscodedPath(Long videoId, String outputDir, String defaultPreset) {
        try {
            Video video = videoRepository.findById(videoId).orElse(null);
            if (video != null) {
                video.setTranscodedDir(outputDir);
                videoRepository.save(video);
                logger.info("[Consumer] Updated transcodedDir in DB for videoId={}", videoId);
            }
        } catch (Exception e) {
            logger.error("[Consumer] Failed to update DB for videoId={}: {}", videoId, e.getMessage());
        }
    }

    private void updateTranscodingStatus(Long videoId, Video.TranscodingStatus status) {
        try {
            Video video = videoRepository.findById(videoId).orElse(null);
            if (video != null) {
                video.setTranscodingStatus(status);
                videoRepository.save(video);
                logger.info("[Consumer] Status updated to {} for videoId={}", status, videoId);
            }
        } catch (Exception e) {
            logger.error("[Consumer] Failed to update status for videoId={}: {}", videoId, e.getMessage());
        }
    }


    private static class VideoConfig {
        int width;
        int height;
        int videoBitrate;
        int audioBitrate;

        VideoConfig(int width, int height, int videoBitrate, int audioBitrate) {
            this.width = width;
            this.height = height;
            this.videoBitrate = videoBitrate;
            this.audioBitrate = audioBitrate;
        }
    }
}