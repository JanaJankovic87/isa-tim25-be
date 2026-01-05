package net.javaguides.springboot_jutjubic.service;

import net.javaguides.springboot_jutjubic.model.Video;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface VideoService {

    Video save(Video videoPost, MultipartFile thumbnail, MultipartFile video) throws Exception;

    Video update(Video videoPost) throws ObjectOptimisticLockingFailureException;

    void delete(Long id);

    List<Video> findAll();

    Video findById(Long id);

    byte[] getThumbnail(Long id) throws IOException;

    void invalidateThumbnailCache(Long id);

    byte[] getVideoFile(Long id) throws IOException;

    List<Video> searchByKeyword(String keyword);

    void likeVideo(Long videoId, Long userId);

    void unlikeVideo(Long videoId, Long userId);

    boolean isVideoLikedByUser(Long videoId, Long userId);

    long getLikesCount(Long videoId);

    List<Video> findByUserId(Long userId);

}
