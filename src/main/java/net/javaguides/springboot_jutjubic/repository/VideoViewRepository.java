package net.javaguides.springboot_jutjubic.repository;

import net.javaguides.springboot_jutjubic.model.VideoView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoViewRepository extends JpaRepository<VideoView, Long> {

    boolean existsByUserIdAndVideoId(Long userId, Long videoId);
    long countByVideoId(Long videoId);
}