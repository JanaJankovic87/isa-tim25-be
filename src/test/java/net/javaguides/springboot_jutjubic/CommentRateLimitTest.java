package net.javaguides.springboot_jutjubic;

import net.javaguides.springboot_jutjubic.dto.CommentDTO;
import net.javaguides.springboot_jutjubic.model.Comment;
import net.javaguides.springboot_jutjubic.model.User;
import net.javaguides.springboot_jutjubic.model.Video;
import net.javaguides.springboot_jutjubic.repository.CommentRepository;
import net.javaguides.springboot_jutjubic.repository.UserRepository;
import net.javaguides.springboot_jutjubic.repository.VideoRepository;
import net.javaguides.springboot_jutjubic.service.CommentService;
import net.javaguides.springboot_jutjubic.service.CommentRateLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CommentRateLimitTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRateLimitService rateLimitService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private CommentRepository commentRepository;

    private User testUser;
    private Video video1;
    private Video video2;
    private Video video3;

    @BeforeEach
    public void setup() {
        commentRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("test_rate_limit_user");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser = userRepository.save(testUser);

        video1 = new Video("Video 1", "Test video 1", null, testUser.getId());
        video1 = videoRepository.save(video1);

        video2 = new Video("Video 2", "Test video 2", null, testUser.getId());
        video2 = videoRepository.save(video2);

        video3 = new Video("Video 3", "Test video 3", null, testUser.getId());
        video3 = videoRepository.save(video3);

        System.out.println("\nSETUP TESTA");
        System.out.println("User ID: " + testUser.getId());
        System.out.println("Video IDs: " + video1.getId() + ", " + video2.getId() + ", " + video3.getId());
        System.out.println("---------------\n");
    }

    @Test
    public void testRateLimitOnSingleVideo() {
        System.out.println("TEST 1: Rate limit na jednom videu");

        int successfulComments = 0;
        int failedComments = 0;

        for (int i = 1; i <= 65; i++) {
            try {

                if (!rateLimitService.canUserComment(testUser.getId())) {
                    throw new RuntimeException("Rate limit exceeded");
                }

                CommentDTO dto = new CommentDTO();
                dto.setText("Test comment #" + i + " at " + LocalDateTime.now());
                dto.setUserId(testUser.getId());

                Comment comment = new Comment();
                comment.setText(dto.getText());
                comment.setUser(testUser);
                comment.setVideo(video1);
                comment.setCreatedAt(LocalDateTime.now());
                commentRepository.save(comment);

                rateLimitService.recordComment(testUser.getId());

                successfulComments++;
                System.out.println("Komentar " + i + " uspešno poslat.");
            } catch (RuntimeException e) {
                failedComments++;
                System.out.println("Komentar " + i + " odbijen: " + e.getMessage());
            }
        }

        System.out.println("Uspešno poslato: " + successfulComments);
        System.out.println("Odbijeno: " + failedComments);

        assertEquals(60, successfulComments, "Tačno 60 komentara treba da bude poslato");
        assertEquals(5, failedComments, "5 komentara treba da bude odbijeno");

        int remaining = rateLimitService.getRemainingComments(testUser.getId());
        assertEquals(0, remaining, "Treba da ostane 0 komentara");
    }

    @Test
    public void testRateLimitAcrossMultipleVideos() {
        System.out.println("TEST 2: Rate limit preko više videa");

        int totalSuccessful = 0;
        int totalFailed = 0;

        for (int i = 1; i <= 25; i++) {
            try {
                if (!rateLimitService.canUserComment(testUser.getId())) {
                    throw new RuntimeException("Rate limit exceeded");
                }

                Comment comment = new Comment();
                comment.setText("Video1 - Comment " + i);
                comment.setUser(testUser);
                comment.setVideo(video1);
                comment.setCreatedAt(LocalDateTime.now());
                commentRepository.save(comment);

                rateLimitService.recordComment(testUser.getId());
                totalSuccessful++;
            } catch (RuntimeException e) {
                totalFailed++;
            }
        }

        for (int i = 1; i <= 25; i++) {
            try {
                if (!rateLimitService.canUserComment(testUser.getId())) {
                    throw new RuntimeException("Rate limit exceeded");
                }

                Comment comment = new Comment();
                comment.setText("Video2 - Comment " + i);
                comment.setUser(testUser);
                comment.setVideo(video2);
                comment.setCreatedAt(LocalDateTime.now());
                commentRepository.save(comment);

                rateLimitService.recordComment(testUser.getId());
                totalSuccessful++;
            } catch (RuntimeException e) {
                totalFailed++;
            }
        }


        for (int i = 1; i <= 20; i++) {
            try {
                if (!rateLimitService.canUserComment(testUser.getId())) {
                    throw new RuntimeException("Rate limit exceeded");
                }

                Comment comment = new Comment();
                comment.setText("Video3 - Comment " + i);
                comment.setUser(testUser);
                comment.setVideo(video3);
                comment.setCreatedAt(LocalDateTime.now());
                commentRepository.save(comment);

                rateLimitService.recordComment(testUser.getId());
                totalSuccessful++;
            } catch (RuntimeException e) {
                totalFailed++;
            }
        }

        System.out.println("Ukupno uspešno poslato: " + totalSuccessful);
        System.out.println("Ukupno odbijeno: " + totalFailed);

        assertEquals(60, totalSuccessful, "Tačno 60 komentara ukupno treba da bude poslato");
        assertEquals(10, totalFailed, "10 komentara treba da bude odbijeno");

        long commentsOnVideo1 = commentRepository.findByVideoIdOrderByCreatedAtDesc(
                video1.getId(), org.springframework.data.domain.PageRequest.of(0, 100)
        ).getTotalElements();
        long commentsOnVideo2 = commentRepository.findByVideoIdOrderByCreatedAtDesc(
                video2.getId(), org.springframework.data.domain.PageRequest.of(0, 100)
        ).getTotalElements();
        long commentsOnVideo3 = commentRepository.findByVideoIdOrderByCreatedAtDesc(
                video3.getId(), org.springframework.data.domain.PageRequest.of(0, 100)
        ).getTotalElements();

        assertEquals(60, commentsOnVideo1 + commentsOnVideo2 + commentsOnVideo3,
                "Ukupno 60 komentara treba da bude u bazi");
    }

    @Test
    public void testConcurrentComments() {
        System.out.println("TEST 3: Stress test - 70 pokušaja");

        int successful = 0;
        int failed = 0;

        for (int i = 1; i <= 70; i++) {
            try {
                if (!rateLimitService.canUserComment(testUser.getId())) {
                    throw new RuntimeException("Rate limit exceeded");
                }

                Comment comment = new Comment();
                comment.setText("Stress test comment " + i);
                comment.setUser(testUser);
                comment.setVideo(video1);
                comment.setCreatedAt(LocalDateTime.now());
                commentRepository.save(comment);

                rateLimitService.recordComment(testUser.getId());
                successful++;
            } catch (RuntimeException e) {
                failed++;
            }
        }

        System.out.println("Uspešno poslato: " + successful);
        System.out.println("Odbijeno: " + failed);

        assertEquals(60, successful, "Tačno 60 komentara treba da uspe");
    }

    @Test
    public void testRateLimitReset() {
        System.out.println("TEST 4: Reset rate limita nakon 1h");

        for (int i = 1; i <= 60; i++) {
            Comment comment = new Comment();
            comment.setText("Old comment " + i);
            comment.setUser(testUser);
            comment.setVideo(video1);
            comment.setCreatedAt(LocalDateTime.now());
            commentRepository.save(comment);
        }

        int remaining = rateLimitService.getRemainingComments(testUser.getId());
        assertEquals(0, remaining, "Limit treba da je iscrpljen");

        LocalDateTime twoHoursAgo = LocalDateTime.now().minusHours(2);
        commentRepository.findAll().forEach(comment -> {
            if (comment.getUser().getId().equals(testUser.getId())) {
                comment.setCreatedAt(twoHoursAgo);
                commentRepository.save(comment);
            }
        });

        remaining = rateLimitService.getRemainingComments(testUser.getId());
        assertEquals(60, remaining, "Limit treba da bude resetovan na 60");

        try {
            if (!rateLimitService.canUserComment(testUser.getId())) {
                fail("Komentar treba da bude dozvoljen nakon reseta");
            }

            Comment comment = new Comment();
            comment.setText("New comment after reset");
            comment.setUser(testUser);
            comment.setVideo(video1);
            comment.setCreatedAt(LocalDateTime.now());
            commentRepository.save(comment);

            rateLimitService.recordComment(testUser.getId());
        } catch (RuntimeException e) {
            fail("Komentar treba da bude dozvoljen nakon reseta: " + e.getMessage());
        }
    }

    @Test
    public void testRemainingCommentsAccuracy() {
        System.out.println("TEST 5: Provera remaining comments");

        for (int i = 1; i <= 60; i++) {
            int remainingBefore = rateLimitService.getRemainingComments(testUser.getId());
            System.out.println("Pre komentara " + i + ": Preostalo " + remainingBefore + "/60");

            Comment comment = new Comment();
            comment.setText("Comment " + i);
            comment.setUser(testUser);
            comment.setVideo(video1);
            comment.setCreatedAt(LocalDateTime.now());
            commentRepository.save(comment);

            int remainingAfter = rateLimitService.getRemainingComments(testUser.getId());
            System.out.println("Posle komentara " + i + ": Preostalo " + remainingAfter + "/60");

            assertEquals(60 - i, remainingAfter, "Treba da preostane " + (60 - i) + " komentara");
        }

        int finalRemaining = rateLimitService.getRemainingComments(testUser.getId());
        assertEquals(0, finalRemaining, "Treba da ostane 0");
    }
}