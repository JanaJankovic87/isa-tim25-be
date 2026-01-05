package net.javaguides.springboot_jutjubic.repository;

import net.javaguides.springboot_jutjubic.model.VideoLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoLikeRepository extends JpaRepository<VideoLike, Long> {

    Optional<VideoLike> findByUserIdAndVideoId(Long userId, Long videoId);

    boolean existsByUserIdAndVideoId(Long userId, Long videoId);

    long countByVideoId(Long videoId);

    List<VideoLike> findByVideoId(Long videoId);

    List<VideoLike> findByUserId(Long userId);

    void deleteByUserIdAndVideoId(Long userId, Long videoId);
}

