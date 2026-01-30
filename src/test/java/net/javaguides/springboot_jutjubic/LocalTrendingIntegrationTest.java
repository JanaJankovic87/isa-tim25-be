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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
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

    private static final StringBuilder finalReport = new StringBuilder();


    @BeforeAll
    public static void setupClass() throws IOException {
        System.out.println(" STARTING LOCAL TRENDING TESTS");
        Files.createDirectories(Paths.get("target"));

        finalReport.append("LOCAL TRENDING - TEST RESULTS SUMMARY\n");
    }

    @BeforeEach
    public void setup() {

        trendingService.resetMetrics();
    }

    @AfterAll
    public static void teardownClass() throws IOException {
        System.out.println(" ALL TESTS COMPLETED");

        Files.write(Paths.get("target/final-test-results.txt"), finalReport.toString().getBytes());
        System.out.println("\n Final results: target/final-test-results.txt");
    }

    //  TEST 1: Basic Functionality

    @Test
    @Order(1)
    @DisplayName("Test 1: Basic trending works with existing data")
    public void testBasicTrendingWithExistingData() {
        System.out.println("\n TEST 1: Basic Trending Functionality");

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
        finalReport.append(String.format("TEST 1 - Basic Functionality: %d videos, %dms\n",
                result.getVideos().size(), result.getResponseTimeMs()));
    }

    // TEST 2: Neighbors in same street
    @Test
    @Order(2)
    @DisplayName("Test 2: Neighbors in same street - trending comparison")
    public void testNeighborsInSameStreet() {
        System.out.println("\n TEST 2: Neighbors in Same Street");


        LocationDTO neighborA = new LocationDTO(44.7866, 20.4489, false);
        neighborA.setLocationName("Komšija A - Bulevar 73");

        LocationDTO neighborB = new LocationDTO(44.7867, 20.4490, false);
        neighborB.setLocationName("Komšija B - Bulevar 75");

        double distance = testDataGenerator.haversine(
                neighborA.getLatitude(), neighborA.getLongitude(),
                neighborB.getLatitude(), neighborB.getLongitude()
        );

        LocalTrendingService.TrendingResult trendingA =
                trendingService.getRealTimeTrending(neighborA, 50, 10);

        LocalTrendingService.TrendingResult trendingB =
                trendingService.getRealTimeTrending(neighborB, 50, 10);



        System.out.println("\n PROVERA 1: Broj videja");
        assertEquals(trendingA.getVideos().size(), trendingB.getVideos().size(),
                "Komšije treba da dobiju ISTI broj videja");




        System.out.println("\n PROVERA 2: Identičnost video zapisa");
        int sameVideos = 0;

        for (int i = 0; i < Math.min(trendingA.getVideos().size(), trendingB.getVideos().size()); i++) {
            Long idA = trendingA.getVideos().get(i).getVideoId();
            Long idB = trendingB.getVideos().get(i).getVideoId();

            assertEquals(idA, idB,
                    "Video na poziciji " + (i+1) + " treba da bude ISTI");

            if (idA.equals(idB)) {
                sameVideos++;
            }
        }


        System.out.println("\n PROVERA 3: Score razlika");
        double totalDiff = 0;
        double maxDiff = 0;

        for (int i = 0; i < Math.min(trendingA.getVideos().size(), trendingB.getVideos().size()); i++) {
            double scoreA = trendingA.getVideos().get(i).getTrendingScore();
            double scoreB = trendingB.getVideos().get(i).getTrendingScore();
            double diff = Math.abs(scoreA - scoreB);
            double percentDiff = scoreA > 0 ? (diff / scoreA) * 100 : 0;

            totalDiff += percentDiff;
            maxDiff = Math.max(maxDiff, percentDiff);
        }

        double avgDiff = trendingA.getVideos().size() > 0 ? totalDiff / trendingA.getVideos().size() : 0;


        assertTrue(avgDiff < 1.0,
                "Prosečna razlika u score-u treba da bude < 1% (actual: " + avgDiff + "%)");


        finalReport.append(String.format("TEST 2 - Neighbors: %d videos, %.1fm apart, %.3f%% score diff\n",
                trendingA.getVideos().size(), distance * 1000, avgDiff));
    }

    //TEST 3: Concentrated Scenario

    @Test
    @Order(3)
    public void testConcentratedScenario() {
        System.out.println("\n TEST 3: Concentrated Scenario");

        testDataGenerator.generateConcentratedScenario();
        LocationDTO location = new LocationDTO(44.7866, 20.4489, false);

        LocalTrendingService.TrendingResult result3km =
                trendingService.getRealTimeTrending(location, 3, 100);

        LocalTrendingService.TrendingResult result10km =
                trendingService.getRealTimeTrending(location, 10, 100);


        assertTrue(result3km.getVideos().size() > 0, "3km mora imati videje");


        assertTrue(result10km.getVideos().size() >= result3km.getVideos().size(),
                String.format("10km (%d) >= 3km (%d)", result10km.getVideos().size(), result3km.getVideos().size()));

        finalReport.append(String.format("TEST 3 - Concentrated: 3km=%d, 10km=%d\n",
                result3km.getVideos().size(), result10km.getVideos().size()));

        testDataGenerator.cleanupTestData();
    }

    // TEST 4: Distributed Scenario

    @Test
    @Order(4)
    @DisplayName("Test 4: Distributed activities (Balkan region)")
    public void testDistributedScenario() {
        System.out.println("\nTEST 4: Distributed Scenario");

        testDataGenerator.generateDistributedScenario();

        // BEOGRAD (centar Srbije)
        LocationDTO belgrade = new LocationDTO(44.7866, 20.4489, false);
        LocalTrendingService.TrendingResult belgradeTrending =
                trendingService.getRealTimeTrending(belgrade, 200, 100);

        // ZAGREB (300km od Beograda)
        LocationDTO zagreb = new LocationDTO(45.8150, 15.9819, false);
        LocalTrendingService.TrendingResult zagrebTrending =
                trendingService.getRealTimeTrending(zagreb, 200, 100);

        System.out.println(" Beograd (200km): " + belgradeTrending.getVideos().size() + " videos");
        System.out.println(" Zagreb (200km): " + zagrebTrending.getVideos().size() + " videos");

        assertNotNull(belgradeTrending.getVideos());
        assertNotNull(zagrebTrending.getVideos());


        assertNotEquals(belgradeTrending.getVideos().size(), zagrebTrending.getVideos().size(),
                "Beograd i Zagreb treba da imaju razliciti broj trending videa");

        System.out.println(" Different cities return different trending");

        finalReport.append(String.format("TEST 4 - Distributed: Beograd=%d, Zagreb=%d (different)\n",
                belgradeTrending.getVideos().size(), zagrebTrending.getVideos().size()));

        testDataGenerator.cleanupTestData();
    }

    // TEST 5: Performance Benchmark

    @Test
    @Order(5)
    @DisplayName("Test 5: Performance benchmark (100 requests)")
    public void testPerformanceBenchmark() {
        System.out.println("\n TEST 5: Performance Benchmark");

        testDataGenerator.generateConcentratedScenario();


        LocationDTO location = new LocationDTO(44.7866, 20.4489, false);
        int iterations = 100;

        trendingService.resetMetrics();

        String[] strategies = {"REAL_TIME", "CACHED_30S", "CACHED_60S", "CACHED_5MIN"};

        finalReport.append("\nTEST 5 - Performance Benchmark (100 iterations):\n");

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

            finalReport.append(String.format("  %s: %.2fms avg\n", strategy, avgTime));
        }

        testDataGenerator.cleanupTestData();

        LocalTrendingService.PerformanceMetrics metrics = trendingService.getMetrics();

        System.out.println("\n Final Metrics:");
        System.out.println("Cache Hit Rate: " + String.format("%.1f", metrics.getCacheHitRate()) + "%");

        finalReport.append(String.format("  Cache Hit Rate: %.1f%%\n", metrics.getCacheHitRate()));

        assertTrue(metrics.getCacheHitRate() > 70.0, "Cache hit rate > 70%");
    }
    // TEST 6: Different Radius

    @Test
    @Order(6)
    @DisplayName("Test 6: Different radius values")
    public void testDifferentRadii() {
        System.out.println("\n TEST 6: Different Radius Values");

        testDataGenerator.generateDistributedScenario();  // 96 videa sirom Balkana

        LocationDTO location = new LocationDTO(44.7866, 20.4489, false);  // Beograd
        int[] radii = {50, 100, 200, 500};

        finalReport.append("\nTEST 6 - Different Radius Values:\n");

        int previousCount = 0;

        for (int radius : radii) {
            LocalTrendingService.TrendingResult result =
                    trendingService.getRealTimeTrending(location, radius, 100);

            int currentCount = result.getVideos().size();

            System.out.println(String.format("  Radius %dkm: %d videos (%dms)",
                    radius, currentCount, result.getResponseTimeMs()));

            finalReport.append(String.format("  %dkm: %d videos, %dms\n",
                    radius, currentCount, result.getResponseTimeMs()));


            assertTrue(currentCount >= previousCount,
                    String.format("Radius %dkm should return >= videos than previous radius", radius));

            previousCount = currentCount;

            assertTrue(result.getResponseTimeMs() < 500, "Response time < 500ms");
        }

        testDataGenerator.cleanupTestData();
    }

    // TEST 7: Edge Cases

    @Test
    @Order(7)
    @DisplayName("Test 7: Edge cases and error handling")
    public void testEdgeCases() {
        System.out.println("\n TEST 7: Edge Cases");

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

        finalReport.append("TEST 7 - Edge Cases: All handled correctly\n");
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

        finalReport.append(String.format("TEST 8 - Concurrent Operations: 20 ops in %dms (no blocking)\n", totalTime));

        assertTrue(totalTime < 10000,
                "Total time should be < 10s (actual: " + totalTime + "ms)");
    }




}