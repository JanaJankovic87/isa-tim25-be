package net.javaguides.springboot_jutjubic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import net.javaguides.springboot_jutjubic.model.Video;

import java.util.List;

    @Repository
    public interface VideoRepository extends JpaRepository<Video, Long> {
        List<Video> findByUserId(Long userId);
        List<Video> findAll();
        List<Video> findByTitleContainingIgnoreCase(String title);
        List<Video> findAllByOrderByCreatedAtDesc();
    }


