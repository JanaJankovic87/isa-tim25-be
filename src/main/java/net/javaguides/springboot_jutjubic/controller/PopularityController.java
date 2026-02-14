package net.javaguides.springboot_jutjubic.controller;

import net.javaguides.springboot_jutjubic.dto.VideoPopularityDTO;
import net.javaguides.springboot_jutjubic.model.PopularityResult;
import net.javaguides.springboot_jutjubic.service.PopularityETLService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/popularity")
@CrossOrigin(origins = "http://localhost:4200")
public class PopularityController {

    private static final Logger logger = LoggerFactory.getLogger(PopularityController.class);

    @Autowired
    private PopularityETLService popularityETLService;

    // GET /api/popularity/top-videos
    // top 3 popular videos sa kompletnim detaljima za prikaz
    @GetMapping("/top-videos")
    public ResponseEntity<?> getTopVideos() {
        try {
            List<VideoPopularityDTO> topVideos = popularityETLService.getTopVideosForDisplay();

            if (topVideos.isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "No popularity data available yet. Pipeline needs to run first.");
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.ok(topVideos);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch popular videos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // POST /api/popularity/run-pipeline
    // manual trigger pipeline-a
    @PostMapping("/run-pipeline")
    public ResponseEntity<?> runPipelineManually() {
        try {
            popularityETLService.runManually();

            Map<String, String> response = new HashMap<>();
            response.put("message", "ETL pipeline executed successfully");
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to run pipeline: " + e.getMessage());
            error.put("status", "failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // GET /api/popularity/health
    // health check endpoint za proveru statusa servisa
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "Popularity ETL Service");

        // Proveri koliko ima rezultata u bazi
        List<VideoPopularityDTO> topVideos = popularityETLService.getTopVideosForDisplay();
        response.put("topVideosCount", topVideos.size());
        response.put("message", topVideos.isEmpty()
            ? "No data available. Run pipeline first."
            : "Data available - " + topVideos.size() + " top videos");

        return ResponseEntity.ok(response);
    }

    // GET /api/popularity/debug
    // DEBUG endpoint - Direktna provera tabele POPULARITY_RESULTS
    @GetMapping("/debug")
    public ResponseEntity<?> debugResults() {
        try {
            List<PopularityResult> allResults = popularityETLService.getTopVideos();

            Map<String, Object> response = new HashMap<>();
            response.put("totalResults", allResults.size());
            response.put("results", allResults);

            if (allResults.isEmpty()) {
                response.put("message", "POPULARITY_RESULTS table is EMPTY! Pipeline not executed or failed.");
            } else {
                response.put("message", "Found " + allResults.size() + " results in database");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("message", "Error reading from database");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}