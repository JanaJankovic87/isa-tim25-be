package net.javaguides.springboot_jutjubic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.javaguides.springboot_jutjubic.config.RabbitMQConfig;
import net.javaguides.springboot_jutjubic.messages.UploadEventDto;
import net.javaguides.springboot_jutjubic.messages.UploadEventProto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class UploadEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public UploadEventPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishJson(UploadEventDto eventDto) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(eventDto);
            rabbitTemplate.convertAndSend(RabbitMQConfig.JSON_QUEUE, jsonMessage);
            System.out.println("✅ Published JSON: " + eventDto.getTitle());
        } catch (Exception e) {
            System.err.println("❌ Error publishing JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void publishProtobuf(UploadEventProto.UploadEvent event) {
        try {
            byte[] protobufMessage = event.toByteArray();
            rabbitTemplate.convertAndSend(RabbitMQConfig.PROTOBUF_QUEUE, protobufMessage);
            System.out.println("✅ Published Protobuf: " + event.getTitle());
        } catch (Exception e) {
            System.err.println("❌ Error publishing Protobuf: " + e.getMessage());
            e.printStackTrace();
        }
    }
}