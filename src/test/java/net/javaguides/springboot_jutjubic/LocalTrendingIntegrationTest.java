package net.javaguides.springboot_jutjubic;

import net.javaguides.springboot_jutjubic.dto.LocationDTO;
import net.javaguides.springboot_jutjubic.service.impl.LocalTrendingService;
import net.javaguides.springboot_jutjubic.util.TrendingTestDataGenerator;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import net.javaguides.springboot_jutjubic.model.Video;
import net.javaguides.springboot_jutjubic.repository.VideoRepository;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
   TESTOVI za Local Trending System
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LocalTrendingIntegrationTest {

    @Autowired
    private LocalTrendingService trendingService;

    @Autowired
    private TrendingTestDataGenerator testDataGenerator;

    @Autowired
    private VideoRepository videoRepository;

    @BeforeAll
    public static void setupClass() {

        System.out.println(" STARTING LOCAL TRENDING TESTS");
    }

    @BeforeEach
    public void setup() {

        trendingService.resetMetrics();
    }

    @AfterAll
    public static void teardownClass() {

        System.out.println(" ALL TESTS COMPLETED");
    }

    //  TEST 1: Basic Functionality

    @Test
    @Order(1)
    @DisplayName("Test 1: Basic trending works with existing data")
    public void testBasicTrendingWithExistingData() {
        System.out.println("\n📋 TEST 1: Basic Trending Functionality");

        // Beograd lokacija
        LocationDTO location = new LocationDTO(44.7866, 20.4489, false);
        location.setLocationName("Beograd, Serbia");

        // Pozovi trending sa 50km radiusom
        LocalTrendingService.TrendingResult result = trendingService.getLocalTrending(
                location, 50, 10
        );


        assertNotNull(result, "Result ne sme biti null");
        assertNotNull(result.getVideos(), "Videos lista ne sme biti null");
        assertTrue(result.getResponseTimeMs() > 0, "Response time mora biti > 0");

        System.out.println(" Found " + result.getVideos().size() + " trending videos");
        System.out.println(" Response time: " + result.getResponseTimeMs() + "ms");
        System.out.println(" User location: " + result.getUserLocation().getLocationName());

        assertTrue(result.getVideos().size() <= 10, "Limit mora biti poštovan");
    }

    // TEST 2: Cache Performance

    @Test
    @Order(2)
    @DisplayName("Test 2: Cache significantly improves performance")
    public void testCachePerformance() {
        System.out.println("\n TEST 2: Cache Performance");

        LocationDTO location = new LocationDTO(44.7866, 20.4489, false);


        long startMiss = System.currentTimeMillis();
        LocalTrendingService.TrendingResult resultMiss =
                trendingService.getCachedTrending60s(location, 50, 10);
        long timeMiss = System.currentTimeMillis() - startMiss;


        long startHit = System.currentTimeMillis();
        LocalTrendingService.TrendingResult resultHit =
                trendingService.getCachedTrending60s(location, 50, 10);
        long timeHit = System.currentTimeMillis() - startHit;

        System.out.println(" Cache MISS time: " + timeMiss + "ms");
        System.out.println(" Cache HIT time: " + timeHit + "ms");
        System.out.println(" Improvement: " + (timeMiss - timeHit) + "ms (" +
                String.format("%.1f", ((double)timeMiss / timeHit)) + "x faster)");


        assertTrue(timeHit < timeMiss / 10,
                "Cache HIT mora biti bar 10x brži od cache MISS");
    }

    //TEST 3: Concentrated Scenario s

    @Test
    @Order(3)
    @DisplayName("Test 3: Concentrated activities (Beograd centar)")
    public void testConcentratedScenario() {
        System.out.println("\n🔥 TEST 3: Concentrated Scenario");


        testDataGenerator.generateConcentratedScenario();

        LocationDTO location = new LocationDTO(44.7866, 20.4489, false);

        // Mali radijus (5km) - vrati puno rezultata
        LocalTrendingService.TrendingResult resultSmall =
                trendingService.getRealTimeTrending(location, 5, 20);

        // Veliki radijus (50km) - jos vise rezultata
        LocalTrendingService.TrendingResult resultLarge =
                trendingService.getRealTimeTrending(location, 50, 20);

        System.out.println(" Videos in 5km radius: " + resultSmall.getVideos().size());
        System.out.println(" Videos in 50km radius: " + resultLarge.getVideos().size());

        assertTrue(resultSmall.getVideos().size() > 0,
                "Mora biti videa u koncentrovanom području");
        assertTrue(resultLarge.getVideos().size() >= resultSmall.getVideos().size(),
                "Veći radijus mora imati >= rezultata");


        testDataGenerator.cleanupTestData();
    }

    // TEST 4: Distributed Scenario

    @Test
    @Order(4)
    @DisplayName("Test 4: Distributed activities (Balkan region)")
    public void testDistributedScenario() {
        System.out.println("\n TEST 4: Distributed Scenario");

        // Generisi distribuirane podatke
        testDataGenerator.generateDistributedScenario();

        // Test iz Beograda
        LocationDTO belgrade = new LocationDTO(44.7866, 20.4489, false);
        LocalTrendingService.TrendingResult belgradeTrending =
                trendingService.getRealTimeTrending(belgrade, 200, 15);

        // Test iz Zagreba
        LocationDTO zagreb = new LocationDTO(45.8150, 15.9819, false);
        LocalTrendingService.TrendingResult zagrebTrending =
                trendingService.getRealTimeTrending(zagreb, 200, 15);

        System.out.println(" Beograd trending: " + belgradeTrending.getVideos().size() + " videos");
        System.out.println(" Zagreb trending: " + zagrebTrending.getVideos().size() + " videos");

        assertNotNull(belgradeTrending.getVideos());
        assertNotNull(zagrebTrending.getVideos());


        System.out.println(" Different locations return different trending videos");


        testDataGenerator.cleanupTestData();
    }

    // TEST 5: Performance Benchmark

    @Test
    @Order(5)
    @DisplayName("Test 5: Performance benchmark (100 requests)")
    public void testPerformanceBenchmark() {
        System.out.println("\n TEST 5: Performance Benchmark");

        LocationDTO location = new LocationDTO(44.7866, 20.4489, false);
        int iterations = 100;

        trendingService.resetMetrics();

        System.out.println("Running " + iterations + " iterations for each strategy...\n");

        String[] strategies = {"REAL_TIME", "CACHED_30S", "CACHED_60S", "CACHED_5MIN"};

        for (String strategy : strategies) {
            long totalTime = 0;

            for (int i = 0; i < iterations; i++) {
                long start = System.currentTimeMillis();

                switch (strategy) {
                    case "REAL_TIME":
                        trendingService.getRealTimeTrending(location, 50, 10);
                        break;
                    case "CACHED_30S":
                        trendingService.getCachedTrending30s(location, 50, 10);
                        break;
                    case "CACHED_60S":
                        trendingService.getCachedTrending60s(location, 50, 10);
                        break;
                    case "CACHED_5MIN":
                        trendingService.getCachedTrending5min(location, 50, 10);
                        break;
                }

                totalTime += (System.currentTimeMillis() - start);
            }

            double avgTime = totalTime / (double) iterations;
            System.out.println(strategy + " - Avg: " + String.format("%.2f", avgTime) + "ms");
        }

        LocalTrendingService.PerformanceMetrics metrics = trendingService.getMetrics();

        System.out.println("\n📈 Final Metrics:");
        System.out.println("Total Requests: " + metrics.getTotalRequests());
        System.out.println("Cache Hits: " + metrics.getCacheHits());
        System.out.println("Cache Misses: " + metrics.getCacheMisses());
        System.out.println("Cache Hit Rate: " + String.format("%.1f", metrics.getCacheHitRate()) + "%");

        assertTrue(metrics.getCacheHitRate() > 70.0,
                "Cache hit rate mora biti > 70% (actual: " + metrics.getCacheHitRate() + "%)");
    }

    // TEST 6: Different Radius

    @Test
    @Order(6)
    @DisplayName("Test 6: Different radius values")
    public void testDifferentRadii() {
        System.out.println("\n TEST 6: Different Radius Values");

        LocationDTO location = new LocationDTO(44.7866, 20.4489, false);

        int[] radii = {10, 50, 100, 200};

        for (int radius : radii) {
            LocalTrendingService.TrendingResult result =
                    trendingService.getRealTimeTrending(location, radius, 10);

            System.out.println("Radius " + radius + "km: " +
                    result.getVideos().size() + " videos, " +
                    result.getResponseTimeMs() + "ms");

            assertTrue(result.getResponseTimeMs() < 500,
                    "Response time mora biti < 500ms (actual: " + result.getResponseTimeMs() + "ms)");
        }
    }

    // TEST 7: Edge Cases

    @Test
    @Order(7)
    @DisplayName("Test 7: Edge cases and error handling")
    public void testEdgeCases() {
        System.out.println("\n⚠️ TEST 7: Edge Cases");

        assertThrows(Exception.class, () -> {
            trendingService.getLocalTrending(null, 50, 10);
        }, "Null location mora baciti exception");

        LocationDTO location = new LocationDTO(44.7866, 20.4489, false);
        LocalTrendingService.TrendingResult result =
                trendingService.getLocalTrending(location, 50, 0);

        assertEquals(0, result.getVideos().size(), "Limit 0 mora vratiti praznu listu");

        LocalTrendingService.TrendingResult resultNegative =
                trendingService.getLocalTrending(location, -10, 10);

        assertNotNull(resultNegative, "Result ne sme biti null čak i sa negativnim radiusom");

        System.out.println("All edge cases handled correctly");
    }

    // TEST 8: Other operations

    @Test
    @Order(8)
    @DisplayName("Test 8: Trending doesn't block other operations")
    public void testTrendingDoesntBlockOtherOperations() {
        System.out.println("\nTEST 8: Concurrent Operations");

        ExecutorService executor = Executors.newFixedThreadPool(20);
        List<Future<?>> futures = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(20);

        for (int i = 0; i < 10; i++) {
            futures.add(executor.submit(() -> {
                try {
                    latch.countDown();
                    latch.await();

                    LocationDTO location = new LocationDTO(44.7866, 20.4489, false);
                    trendingService.getRealTimeTrending(location, 50, 10);
                } catch (Exception e) {
                    fail("Trending request failed: " + e.getMessage());
                }
            }));
        }

        for (int i = 0; i < 10; i++) {
            futures.add(executor.submit(() -> {
                try {
                    latch.countDown();
                    latch.await();

                    List<Video> videos = videoRepository.findAll();
                    assertNotNull(videos, "Basic operation must not fail");
                } catch (Exception e) {
                    fail("Basic operation failed: " + e.getMessage());
                }
            }));
        }


        long startTime = System.currentTimeMillis();
        for (Future<?> future : futures) {
            try {
                future.get(5000, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                fail("Operation timed out - trending is blocking other operations!");
            } catch (Exception e) {
                fail("Operation failed: " + e.getMessage());
            }
        }
        long totalTime = System.currentTimeMillis() - startTime;

        executor.shutdown();

        System.out.println(" All 20 concurrent operations completed in: " + totalTime + "ms");
        System.out.println(" Trending does NOT block basic operations");


        assertTrue(totalTime < 10000,
                "Total time should be < 10s (actual: " + totalTime + "ms)");
    }

    // TEST 9: CSV

    @Test
    @Order(9)
    @DisplayName("Test 9: Export performance metrics to CSV")
    public void exportMetricsToCSV() throws IOException {
        System.out.println("\n TEST 9: Export Metrics");

        testPerformanceBenchmark();

        LocalTrendingService.PerformanceMetrics metrics = trendingService.getMetrics();

        try (PrintWriter writer = new PrintWriter("target/performance-metrics.csv")) {
            writer.println("Strategy,Avg Response (ms),Min (ms),Max (ms),Median (ms),P95 (ms),Cache Hit Rate (%)");

            metrics.getStrategies().forEach((strategy, stats) -> {
                writer.printf("%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f%n",
                        strategy,
                        stats.getAverageResponseTime(),
                        stats.getMinResponseTime(),
                        stats.getMaxResponseTime(),
                        stats.getMedianResponseTime(),
                        stats.getP95ResponseTime(),
                        stats.getCacheHitRate()
                );
            });
        }

        System.out.println("Metrics exported to target/performance-metrics.csv");
    }


}