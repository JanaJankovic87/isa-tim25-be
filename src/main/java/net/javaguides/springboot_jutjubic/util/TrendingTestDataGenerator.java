package net.javaguides.springboot_jutjubic.util;

import net.javaguides.springboot_jutjubic.model.*;
import net.javaguides.springboot_jutjubic.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 - Generator test podataka za trending testove

  Scenariji:
  1. Koncentrisane aktivnosti:Beograd centar - mali region, puno aktivnosti
  2. Distribuirane aktivnosti:ceo Balkan - veliki region, rasprsene aktivnosti
 */
@Component
public class TrendingTestDataGenerator {

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private VideoLikeRepository videoLikeRepository;

    @Autowired
    private VideoViewRepository videoViewRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    private final Random random = new Random();

    /**
     * SCENARIO 1: Veliki broj aktivnosti u malom prostoru
     * - 50 videa u radijusu od 5km oko Beograda
     * - 500 lajkova koncentrisanih tu
     * - 300 komentara
     * - 800 viewova
     */
    @Transactional
    public void generateConcentratedScenario() {
        System.out.println(" Generating CONCENTRATED scenario Beograd centar");

        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new RuntimeException("Nema korisnika u bazi! Pokreni data.sql prvo.");
        }

        // Beograd centar koordinate
        double centerLat = 44.7866;
        double centerLng = 20.4489;
        double radiusKm = 5.0;

        //  Kreiraj 50 videa
        List<Video> videos = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            Video video = new Video(
                    "Trending Test Video " + i,
                    "Test opis za video " + i + " u Beogradu",
                    List.of("test", "beograd", "trending"),
                    users.get(random.nextInt(users.size())).getId()
            );

            // Random lokacija u radijusu od 5km oko centra
            double[] coords = randomLocationInRadius(centerLat, centerLng, radiusKm);
            video.setLatitude(coords[0]);
            video.setLongitude(coords[1]);
            video.setLocation("Beograd, Serbia");
            video.setIsLocationApproximated(false);
            video.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(30)));

            videos.add(videoRepository.save(video));
        }

        System.out.println(" Created " + videos.size() + " videos");

        //  Generisi 500 lajkova
        generateInteractions(videos, users, 500, centerLat, centerLng, radiusKm, "LIKE");

        //  Generisi 800 viewova
        generateInteractions(videos, users, 800, centerLat, centerLng, radiusKm, "VIEW");

        //  Generisi 300 komentara
        generateInteractions(videos, users, 300, centerLat, centerLng, radiusKm, "COMMENT");

        System.out.println(" CONCENTRATED scenario generated successfully!");
    }

    /**
     * SCENARIO 2: Distribuirane aktivnosti ceo Balkan
     * - 100 videa širom Balkana
     * - 300 lajkova
     * - 200 komentara
     * - 500 viewova
     */
    @Transactional
    public void generateDistributedScenario() {
        System.out.println("Generating DISTRIBUTED scenario Balkan region");

        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new RuntimeException("Nema korisnika u bazi!");
        }


        double[][] cities = {
                {44.7866, 20.4489}, // Beograd
                {45.2671, 19.8335}, // Novi Sad
                {45.8150, 15.9819}, // Zagreb
                {42.6977, 23.3219}, // Sofija
                {41.9973, 21.4280}, // Skoplje
                {43.8563, 18.4131}, // Sarajevo
                {42.4304, 19.2594}, // Podgorica
                {41.3275, 19.8187}  // Tirana
        };

        List<Video> videos = new ArrayList<>();

        //  Kreira 100 videa rasprsenih po regionu
        for (int i = 1; i <= 100; i++) {
            double[] city = cities[random.nextInt(cities.length)];

            Video video = new Video(
                    "Balkan Video " + i,
                    "Distributed test video " + i,
                    List.of("test", "balkan", "distributed"),
                    users.get(random.nextInt(users.size())).getId()
            );

            // Random lokacija u radijusu od 50km oko grada
            double[] coords = randomLocationInRadius(city[0], city[1], 50.0);
            video.setLatitude(coords[0]);
            video.setLongitude(coords[1]);
            video.setLocation("Balkan Region");
            video.setIsLocationApproximated(true);
            video.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(60)));

            videos.add(videoRepository.save(video));
        }

        System.out.println("Created " + videos.size() + " distributed videos");


        for (double[] city : cities) {
            generateInteractions(videos, users, 40, city[0], city[1], 100.0, "LIKE");
            generateInteractions(videos, users, 60, city[0], city[1], 100.0, "VIEW");
            generateInteractions(videos, users, 25, city[0], city[1], 100.0, "COMMENT");
        }

        System.out.println("DISTRIBUTED scenario generated successfully!");
    }

    /**
     * Generiše interakcije (like, view, comment) na random lokacijama
     */
    private void generateInteractions(List<Video> videos, List<User> users, int count,
                                      double centerLat, double centerLng, double radiusKm,
                                      String type) {
        for (int i = 0; i < count; i++) {
            Video video = videos.get(random.nextInt(videos.size()));
            User user = users.get(random.nextInt(users.size()));
            double[] coords = randomLocationInRadius(centerLat, centerLng, radiusKm);

            try {
                switch (type) {
                    case "LIKE":
                        if (!videoLikeRepository.existsByUserIdAndVideoId(user.getId(), video.getId())) {
                            VideoLike like = new VideoLike(user.getId(), video.getId());
                            like.setLatitude(coords[0]);
                            like.setLongitude(coords[1]);
                            like.setLocationName("Test Location");
                            like.setIsLocationApproximated(false);
                            videoLikeRepository.save(like);
                        }
                        break;

                    case "VIEW":
                        if (!videoViewRepository.existsByUserIdAndVideoId(user.getId(), video.getId())) {
                            VideoView view = new VideoView(user.getId(), video.getId());
                            view.setLatitude(coords[0]);
                            view.setLongitude(coords[1]);
                            view.setLocationName("Test Location");
                            view.setIsLocationApproximated(false);
                            videoViewRepository.save(view);
                        }
                        break;

                    case "COMMENT":
                        Comment comment = new Comment("Test komentar " + i, user, video);
                        comment.setLatitude(coords[0]);
                        comment.setLongitude(coords[1]);
                        comment.setLocationName("Test Location");
                        comment.setIsLocationApproximated(false);
                        commentRepository.save(comment);
                        break;
                }
            } catch (Exception e) {
                // Skip
            }
        }

        System.out.println("Generated " + count + " " + type + " interactions");
    }

    /**
      Generise random koordinate u radijusu oko centra
      Koristi Haversine formulu za tacnost
     */
    private double[] randomLocationInRadius(double centerLat, double centerLng, double radiusKm) {
        // Random distanca u radijusu
        double distance = random.nextDouble() * radiusKm;

        // Random ugao
        double angle = random.nextDouble() * 2 * Math.PI;

        // Konvertuj u latitude/longitude offset
        double latOffset = (distance / 111.0) * Math.cos(angle);
        double lngOffset = (distance / (111.0 * Math.cos(Math.toRadians(centerLat)))) * Math.sin(angle);

        return new double[]{
                centerLat + latOffset,
                centerLng + lngOffset
        };
    }

    /**
     * Briše sve test podatke
     */
    @Transactional
    public void cleanupTestData() {
        System.out.println(" Cleaning up test data");

        commentRepository.deleteAll();
        videoLikeRepository.deleteAll();
        videoViewRepository.deleteAll();

        List<Video> testVideos = videoRepository.findByTitleContainingIgnoreCase("Test");
        videoRepository.deleteAll(testVideos);

        testVideos = videoRepository.findByTitleContainingIgnoreCase("Balkan Video");
        videoRepository.deleteAll(testVideos);

        System.out.println(" Test data cleaned up!");
    }
}