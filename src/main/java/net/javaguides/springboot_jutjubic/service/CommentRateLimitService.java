package net.javaguides.springboot_jutjubic.service;

public interface CommentRateLimitService {
    boolean canUserComment(Long userId);
    int getRemainingComments(Long userId);
}