package net.javaguides.springboot_jutjubic.service;

import net.javaguides.springboot_jutjubic.dto.CommentDTO;
import net.javaguides.springboot_jutjubic.dto.LocationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Locale;

public interface CommentService {
    CommentDTO createComment(Long videoId, CommentDTO commentDTO, Long userId, LocationDTO location);
    Page<CommentDTO> getCommentsByVideoId(Long videoId, Pageable pageable);
}