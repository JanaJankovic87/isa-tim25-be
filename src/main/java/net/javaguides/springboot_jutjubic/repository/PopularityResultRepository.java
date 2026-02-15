package net.javaguides.springboot_jutjubic.repository;

import net.javaguides.springboot_jutjubic.model.PopularityResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PopularityResultRepository extends JpaRepository<PopularityResult, Long> {

    List<PopularityResult> findByPipelineRunAtOrderByRankPositionAsc(LocalDateTime pipelineRunAt);
    List<PopularityResult> findByPipelineRunAtBefore(LocalDateTime cutoffDate);
    void deleteByPipelineRunAt(LocalDateTime pipelineRunAt);
}