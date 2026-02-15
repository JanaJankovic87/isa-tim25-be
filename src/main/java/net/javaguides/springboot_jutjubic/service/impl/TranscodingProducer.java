package net.javaguides.springboot_jutjubic.service.impl;

import net.javaguides.springboot_jutjubic.config.TranscodingConfig;
import net.javaguides.springboot_jutjubic.dto.TranscodingMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TranscodingProducer {

    private static final Logger logger = LoggerFactory.getLogger(TranscodingProducer.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${app.transcoding.output-dir:uploads/transcoded}")
    private String outputDir;


    @Value("${app.transcoding.presets:720p,480p}")
    private String presetsConfig;

    public void sendTranscodingRequest(Long videoId, String videoPath) {
        String[] presets = presetsConfig.split(",");

        TranscodingMessage message = new TranscodingMessage(
                videoId,
                videoPath,
                outputDir + "/" + videoId,
                presets
        );

        rabbitTemplate.convertAndSend(
                TranscodingConfig.TRANSCODING_EXCHANGE,
                TranscodingConfig.TRANSCODING_ROUTING_KEY,
                message
        );

        logger.info("Transcoding request sent for videoId={}, path={}", videoId, videoPath);
    }
}