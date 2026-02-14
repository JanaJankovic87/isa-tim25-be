package net.javaguides.springboot_jutjubic.service.impl;

import net.javaguides.springboot_jutjubic.dto.VideoPopularityDTO;
import net.javaguides.springboot_jutjubic.model.PopularityResult;
import net.javaguides.springboot_jutjubic.model.Video;
import net.javaguides.springboot_jutjubic.model.VideoView;
import net.javaguides.springboot_jutjubic.repository.PopularityResultRepository;
import net.javaguides.springboot_jutjubic.repository.VideoLikeRepository;
import net.javaguides.springboot_jutjubic.repository.VideoRepository;
import net.javaguides.springboot_jutjubic.repository.VideoViewRepository;
import net.javaguides.springboot_jutjubic.service.PopularityETLService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PopularityETLServiceImpl implements PopularityETLService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private VideoViewRepository videoViewRepository;

    @Autowired
    private PopularityResultRepository popularityResultRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private VideoLikeRepository videoLikeRepository;

    @Value("${etl.pipeline.days:7}")
    private int daysToAnalyze;

    @Value("${etl.pipeline.top.count:3}")
    private int topCount;

    // Automatski pokreće pipeline pri startu aplikacije ako nema podataka u bazi
    @PostConstruct
    public void initializePopularityData() {
        try {
            // Proveri da li postoje rezultati u bazi
            long count = popularityResultRepository.count();

            if (count == 0) {

                // Pokreni pipeline
                runManually();

            }
        } catch (Exception e) {
            logger.error("Greška pri inicijalnom pokretanju ETL pipeline-a: {}", e.getMessage());
            // Ne bacamo exception da ne bi sprečili pokretanje aplikacije
        }
    }

    @Override
    @Scheduled(cron = "${etl.pipeline.cron:0 00 20 * * ?}")
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class,
            timeout = 60
    )
    public void runDailyETLPipeline() {
        long startTime = System.currentTimeMillis();

        try {
            // izvlacenje podataka
            List<VideoView> recentViews = extractRecentViews();


            if (recentViews.isEmpty()) {
                return;
            }

            // transformacija i računanje
            List<VideoPopularityDTO> popularityScores = transformAndCalculateScores(recentViews);

            // cuvanje rezultata
            int saved = loadTopVideosToDatabase(popularityScores);

            // cleanup starih rezultata
            int cleaned = cleanupOldResults();


            long duration = System.currentTimeMillis() - startTime;


        } catch (Exception e) {
            throw new RuntimeException("ETL Pipeline neuspešan: " + e.getMessage(), e);
        }
    }

    // uzimamo sve pregleda iz poslednjih N dana - filtriranje po datumu u SQL-u
    private List<VideoView> extractRecentViews() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToAnalyze);

        return videoViewRepository.findByViewedAtAfter(cutoffDate);
    }

    // Računamo popularnost na osnovu broja pregleda i njihovog vremena - noviji pregledi imaju veću težinu
    private List<VideoPopularityDTO> transformAndCalculateScores(List<VideoView> views) {
        LocalDate today = LocalDate.now();
        Map<Long, Map<Integer, Long>> videoViewsByDay = new HashMap<>();

        for (VideoView view : views) {
            Long videoId = view.getVideoId();
            LocalDate viewDate = view.getViewedAt().toLocalDate();
            int daysAgo = (int) ChronoUnit.DAYS.between(viewDate, today);

            if (daysAgo >= 0 && daysAgo < daysToAnalyze) {
                videoViewsByDay
                        .computeIfAbsent(videoId, k -> new HashMap<>())
                        .merge(daysAgo, 1L, Long::sum);
            }
        }

        List<VideoPopularityDTO> results = new ArrayList<>();

        for (Map.Entry<Long, Map<Integer, Long>> entry : videoViewsByDay.entrySet()) {
            Long videoId = entry.getKey();
            Map<Integer, Long> viewsByDay = entry.getValue();

            double score = 0.0;
            long totalViews = 0;

            for (Map.Entry<Integer, Long> dayEntry : viewsByDay.entrySet()) {
                int daysAgo = dayEntry.getKey();
                long viewsOnDay = dayEntry.getValue();
                int weight = daysToAnalyze - daysAgo;

                score += viewsOnDay * weight;
                totalViews += viewsOnDay;
            }

            results.add(new VideoPopularityDTO(videoId, score, totalViews));
        }

        // Sortiranje u Java kodu
        Collections.sort(results);

        return results;
    }

    // Čuvamo samo top N videa - ostatak se ignoriše
    private int loadTopVideosToDatabase(List<VideoPopularityDTO> popularityScores) {
        LocalDateTime pipelineRunTime = LocalDateTime.now();
        List<PopularityResult> resultsToSave = new ArrayList<>();

        int rank = 1;
        int limit = Math.min(topCount, popularityScores.size());

        for (int i = 0; i < limit; i++) {
            VideoPopularityDTO dto = popularityScores.get(i);

            PopularityResult result = new PopularityResult(
                    dto.getVideoId(),
                    dto.getPopularityScore(),
                    rank,
                    dto.getTotalViews()
            );
            result.setPipelineRunAt(pipelineRunTime);

            resultsToSave.add(result);



            rank++;
        }

        popularityResultRepository.saveAll(resultsToSave);

        return resultsToSave.size();
    }

    private int cleanupOldResults() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);

        List<PopularityResult> oldResults = popularityResultRepository.findByPipelineRunAtBefore(cutoff);

        if (!oldResults.isEmpty()) {
            popularityResultRepository.deleteAll(oldResults);
            logger.info("  ✓ Obrisano {} starih rezultata", oldResults.size());
            return oldResults.size();
        }

        return 0;
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class,
            timeout = 60
    )
    public void runManually() {
        runDailyETLPipeline();
    }

    // vraca samo rezultate iz poslednjeg izvršavanja pipeline-a - filtriranje po datumu u Java kodu
    // top videi
    @Override
    @Transactional(readOnly = true)
    public List<PopularityResult> getTopVideos() {

        List<PopularityResult> allResults = popularityResultRepository.findAll(
                Sort.by(Sort.Direction.DESC, "pipelineRunAt")
        );


        if (allResults.isEmpty()) {
            return new ArrayList<>();
        }

        // Uzmi najnovije vreme izvršavanja
        LocalDateTime latestRun = allResults.get(0).getPipelineRunAt();

        // Filtriraj i sortiraj rezultate iz najnovijeg izvršavanja, i vrati samo top N
        List<PopularityResult> topResults = allResults.stream()
                .filter(r -> r.getPipelineRunAt().equals(latestRun))
                .sorted(Comparator.comparing(PopularityResult::getRankPosition))
                .limit(topCount)
                .collect(Collectors.toList());

        return topResults;
    }

    // vraca top 3 najpopularnija videa sa kompletnim detaljima za prikaz na frontendu
    @Override
    @Transactional(readOnly = true)
    public List<VideoPopularityDTO> getTopVideosForDisplay() {

        List<PopularityResult> topResults = getTopVideos();

        List<VideoPopularityDTO> dtos = new ArrayList<>();

        for (PopularityResult result : topResults) {

            Video video = videoRepository.findById(result.getVideoId()).orElse(null);

            if (video != null) {

                // Brojanje lajkova za ovaj video
                Long likesCount = videoLikeRepository.countByVideoId(result.getVideoId());

                VideoPopularityDTO dto = new VideoPopularityDTO(
                    result.getVideoId(),
                    video.getTitle(),
                    video.getThumbnailPath(),
                    result.getPopularityScore(),
                    result.getViewCount(),
                    likesCount,
                    video.getLocation(),
                    video.getLatitude(),
                    video.getLongitude()
                );
                dtos.add(dto);
            }
        }

        return dtos;
    }
}