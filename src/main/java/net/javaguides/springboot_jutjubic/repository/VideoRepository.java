package net.javaguides.springboot_jutjubic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import net.javaguides.springboot_jutjubic.model.Video;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    List<Video> findByUserId(Long userId);

    @Query("SELECT DISTINCT v FROM Video v LEFT JOIN FETCH v.tags ORDER BY v.createdAt DESC")
    List<Video> findAllByOrderByCreatedAtDesc();

    @Query("SELECT DISTINCT v FROM Video v LEFT JOIN FETCH v.tags WHERE LOWER(v.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Video> findByTitleContainingIgnoreCase(@Param("title") String title);

    @Query("SELECT DISTINCT v FROM Video v LEFT JOIN FETCH v.tags")
    List<Video> findAll();

    @Query("SELECT v FROM Video v LEFT JOIN FETCH v.comments c LEFT JOIN FETCH c.user WHERE v.id = :id")
    Optional<Video> findByIdWithComments(@Param("id") Long id);
}