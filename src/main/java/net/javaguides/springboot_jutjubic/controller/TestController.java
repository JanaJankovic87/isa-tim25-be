package net.javaguides.springboot_jutjubic.controller;

import net.javaguides.springboot_jutjubic.messages.UploadEventDto;
import net.javaguides.springboot_jutjubic.messages.UploadEventProto;
import net.javaguides.springboot_jutjubic.service.UploadEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "http://localhost:4200")
public class TestController {

    @Autowired(required = false)
    private UploadEventPublisher uploadEventPublisher;

    @GetMapping("/send-message")
    public String testMessage() {
        if (uploadEventPublisher == null) {
            return "UploadEventPublisher is not available!";
        }

        // Kreiraj test event
        UploadEventDto testEvent = new UploadEventDto();
        testEvent.setVideoId("test-123");
        testEvent.setTitle("Test Video");
        testEvent.setFileSize(15000000L); // 15 MB
        testEvent.setAuthorId("author-1");
        testEvent.setAuthorName("Test User");
        testEvent.setThumbnailUrl("http://example.com/thumb.jpg");
        testEvent.setVideoUrl("http://example.com/video.mp4");
        testEvent.setTimestamp(System.currentTimeMillis());
        testEvent.setTags(Arrays.asList("test", "demo", "video"));

        // Pošalji JSON poruku
        uploadEventPublisher.publishJson(testEvent);

        return "Test message sent to RabbitMQ!";
    }

    @GetMapping("/send-multiple/{count}")
    public String testMultiple(@PathVariable int count) {
        if (uploadEventPublisher == null) {
            return "UploadEventPublisher is not available!";
        }

        for (int i = 0; i < count; i++) {
            UploadEventDto testEvent = new UploadEventDto();
            testEvent.setVideoId("test-" + i);
            testEvent.setTitle("Test Video " + i);
            testEvent.setFileSize(10000000L + i * 1000);
            testEvent.setAuthorId("author-" + i);
            testEvent.setAuthorName("User " + i);
            testEvent.setThumbnailUrl("http://example.com/thumb" + i + ".jpg");
            testEvent.setVideoUrl("http://example.com/video" + i + ".mp4");
            testEvent.setTimestamp(System.currentTimeMillis());
            testEvent.setTags(Arrays.asList("test", "video" + i));

            uploadEventPublisher.publishJson(testEvent);

            if (i % 10 == 0) {
                System.out.println("✅ JSON poruka poslata: " + i + "/" + count);
            }
        }

        return "Sent " + count + " JSON messages to RabbitMQ!";
    }

    @GetMapping("/send-multiple-proto/{count}")
    public String testMultipleProto(@PathVariable int count) {
        if (uploadEventPublisher == null) {
            return "UploadEventPublisher is not available!";
        }

        for (int i = 0; i < count; i++) {
            UploadEventDto dto = new UploadEventDto();
            dto.setVideoId("test-proto-" + i);
            dto.setTitle("Proto Test Video " + i);
            dto.setFileSize(10000000L + i * 1000);
            dto.setAuthorId("author-" + i);
            dto.setAuthorName("Proto User " + i);
            dto.setThumbnailUrl("http://example.com/thumb" + i + ".jpg");
            dto.setVideoUrl("http://example.com/video" + i + ".mp4");
            dto.setTimestamp(System.currentTimeMillis());
            dto.setTags(Arrays.asList("proto", "test", "video" + i));

            // Konvertuj u proto i pošalji
            UploadEventProto.UploadEvent protoEvent = mapToProto(dto);
            uploadEventPublisher.publishProtobuf(protoEvent);

            if (i % 10 == 0) {
                System.out.println("✅ PROTO poruka poslata: " + i + "/" + count);
            }
        }

        return "Sent " + count + " PROTO messages to RabbitMQ!";
    }

    @GetMapping("/send-both/{count}")
    public String testBoth(@PathVariable int count) {
        if (uploadEventPublisher == null) {
            return "UploadEventPublisher is not available!";
        }

        for (int i = 0; i < count; i++) {
            UploadEventDto dto = new UploadEventDto();
            dto.setVideoId("test-both-" + i);
            dto.setTitle("Both Formats Test " + i);
            dto.setFileSize(10000000L + i * 1000);
            dto.setAuthorId("author-" + i);
            dto.setAuthorName("User " + i);
            dto.setThumbnailUrl("http://example.com/thumb" + i + ".jpg");
            dto.setVideoUrl("http://example.com/video" + i + ".mp4");
            dto.setTimestamp(System.currentTimeMillis());
            dto.setTags(Arrays.asList("both", "test", "video" + i));

            // Pošalji JSON
            uploadEventPublisher.publishJson(dto);

            // Pošalji PROTO
            UploadEventProto.UploadEvent protoEvent = mapToProto(dto);
            uploadEventPublisher.publishProtobuf(protoEvent);

            if (i % 10 == 0) {
                System.out.println("✅ JSON + PROTO poslato: " + i + "/" + count);
            }
        }

        return "Sent " + count + " messages in BOTH formats (total: " + (count * 2) + " messages)!";
    }

    @GetMapping("/queue-status")
    public String queueStatus() {
        if (uploadEventPublisher == null) {
            return "UploadEventPublisher is not available! Check RabbitMQ connection.";
        }
        return "UploadEventPublisher is working! Ready to send messages.";
    }

    // Helper metoda za konverziju DTO -> Proto
    private UploadEventProto.UploadEvent mapToProto(UploadEventDto dto) {
        return UploadEventProto.UploadEvent.newBuilder()
                .setVideoId(dto.getVideoId())
                .setTitle(dto.getTitle())
                .setFileSize(dto.getFileSize())
                .setAuthorId(dto.getAuthorId())
                .setAuthorName(dto.getAuthorName())
                .setThumbnailUrl(dto.getThumbnailUrl())
                .setVideoUrl(dto.getVideoUrl())
                .setTimestamp(dto.getTimestamp())
                .addAllTags(dto.getTags())
                .build();
    }
}