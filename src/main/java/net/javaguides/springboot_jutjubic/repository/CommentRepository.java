package net.javaguides.springboot_jutjubic.repository;

import net.javaguides.springboot_jutjubic.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByVideoIdOrderByCreatedAtDesc(Long videoId, Pageable pageable);
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.user.id = ?1 AND c.createdAt >= ?2")
    long countUserCommentsInLastHour(Long userId, LocalDateTime oneHourAgo);
    List<Comment> findByVideoId(Long videoId);
}