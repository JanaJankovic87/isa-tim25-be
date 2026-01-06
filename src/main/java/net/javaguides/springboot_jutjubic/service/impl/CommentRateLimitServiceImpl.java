package net.javaguides.springboot_jutjubic.service.impl;

import net.javaguides.springboot_jutjubic.repository.CommentRepository;
import net.javaguides.springboot_jutjubic.service.CommentRateLimitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CommentRateLimitServiceImpl implements CommentRateLimitService {

    private static final int MAX_COMMENTS_PER_HOUR = 60;

    @Autowired
    private CommentRepository commentRepository;

    @Override
    public boolean canUserComment(Long userId) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long commentCount = commentRepository.countUserCommentsInLastHour(userId, oneHourAgo);
        return commentCount < MAX_COMMENTS_PER_HOUR;
    }

    @Override
    public int getRemainingComments(Long userId) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long commentCount = commentRepository.countUserCommentsInLastHour(userId, oneHourAgo);
        return (int) Math.max(0, MAX_COMMENTS_PER_HOUR - commentCount);
    }
}