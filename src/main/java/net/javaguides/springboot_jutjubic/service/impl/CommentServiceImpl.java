    package net.javaguides.springboot_jutjubic.service.impl;

    import net.javaguides.springboot_jutjubic.dto.CommentDTO;
    import net.javaguides.springboot_jutjubic.model.Comment;
    import net.javaguides.springboot_jutjubic.model.User;
    import net.javaguides.springboot_jutjubic.model.Video;
    import net.javaguides.springboot_jutjubic.repository.CommentRepository;
    import net.javaguides.springboot_jutjubic.repository.UserRepository;
    import net.javaguides.springboot_jutjubic.repository.VideoRepository;
    import net.javaguides.springboot_jutjubic.service.CommentRateLimitService;
    import net.javaguides.springboot_jutjubic.service.CommentService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.cache.annotation.CacheEvict;
    import org.springframework.cache.annotation.Cacheable;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    @Service
    public class CommentServiceImpl implements CommentService {

        @Autowired
        private CommentRepository commentRepository;

        @Autowired
        private VideoRepository videoRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private CommentRateLimitService rateLimitService;

        @Override
        @Transactional
        @CacheEvict(value = "videoComments", allEntries = true)
        public CommentDTO createComment(Long videoId, CommentDTO commentDTO, Long userId) {


            if (!rateLimitService.canUserComment(userId)) {
                int remaining = rateLimitService.getRemainingComments(userId);
                throw new RuntimeException(
                        "Comment limit exceeded. You have " + remaining + " comments remaining this hour."
                );
            }


            Video video = videoRepository.findById(videoId)
                    .orElseThrow(() -> new RuntimeException("Video not found with id: " + videoId));


            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));


            Comment comment = new Comment(commentDTO.getText(), user, video);
            Comment savedComment = commentRepository.save(comment);


            rateLimitService.recordComment(userId);

            return convertToDTO(savedComment);
        }

        @Override
        @Cacheable(value = "videoComments", key = "#videoId.toString() + '-' + #pageable.pageNumber.toString()")
        public Page<CommentDTO> getCommentsByVideoId(Long videoId, Pageable pageable) {
            Page<Comment> comments = commentRepository.findByVideoIdOrderByCreatedAtDesc(videoId, pageable);
            return comments.map(this::convertToDTO);
        }

        @Override
        @Transactional
        @CacheEvict(value = "videoComments", allEntries = true)
        public void deleteComment(Long commentId, Long userId) {
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("Comment not found"));


            if (!comment.getUser().getId().equals(userId)) {
                throw new RuntimeException("You can only delete your own comments");
            }

            commentRepository.delete(comment);
        }

        private CommentDTO convertToDTO(Comment comment) {
            return new CommentDTO(
                    comment.getId(),
                    comment.getText(),
                    comment.getUser().getId(),
                    comment.getUser().getUsername(),
                    comment.getUser().getFirstName(),
                    comment.getUser().getLastName(),
                    comment.getCreatedAt(),
                    comment.getVideo().getId()
            );
        }
    }