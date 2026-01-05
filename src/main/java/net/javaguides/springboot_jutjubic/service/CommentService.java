package net.javaguides.springboot_jutjubic.service;

import net.javaguides.springboot_jutjubic.dto.CommentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    CommentDTO createComment(Long videoId, CommentDTO commentDTO, Long userId);
    Page<CommentDTO> getCommentsByVideoId(Long videoId, Pageable pageable);
    void deleteComment(Long commentId, Long userId);
}