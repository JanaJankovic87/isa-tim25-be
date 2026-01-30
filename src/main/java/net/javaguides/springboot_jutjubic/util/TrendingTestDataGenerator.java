package net.javaguides.springboot_jutjubic.util;

import net.javaguides.springboot_jutjubic.model.*;
import net.javaguides.springboot_jutjubic.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
     * SCENARIO 1: KONCENTRISANE AKTIVNOSTI
     * Pravi video snimke na razlicitim udaljenostima sa mnogo interakcija blizu centra
     */
    @Transactional
    public void generateConcentratedScenario() {
        System.out.println(" CONCENTRATED SCENARIO");

        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new RuntimeException("Nema korisnika u bazi!");
        }

        double centerLat = 44.7866;
        double centerLng = 20.4489;
        List<Video> allVideos = new ArrayList<>();

        System.out.println("\n GROUP 1: Creating 20 videos in 0-2.5km (HIGH activity)");
        List<Video> closeVideos = new ArrayList<>();

        for (int i = 1; i <= 20; i++) {
            double[] coords = randomLocationInRing(centerLat, centerLng, 0.0, 2.5);
            double distance = haversine(centerLat, centerLng, coords[0], coords[1]);

            Video video = new Video(
                    String.format("Close Video %02d", i),
                    String.format("Video BLIZU na %.1fkm - HIGH activity", distance),
                    List.of("test", "beograd", "close"),
                    users.get(random.nextInt(users.size())).getId()
            );

            video.setLatitude(coords[0]);
            video.setLongitude(coords[1]);
            video.setLocation(String.format("Beograd Centar (%.1fkm)", distance));
            video.setIsLocationApproximated(false);
            video.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(7)));

            video = videoRepository.save(video);
            closeVideos.add(video);
            allVideos.add(video);

            System.out.printf("   Video %02d: %.2fkm @ (%.4f, %.4f)\n",
                    i, distance, coords[0], coords[1]);
        }

        System.out.println("\n GROUP 2: Creating 15 videos in 6-12km (MEDIUM activity)");
        List<Video> mediumVideos = new ArrayList<>();

        for (int i = 21; i <= 35; i++) {
            double[] coords = randomLocationInRing(centerLat, centerLng, 6.0, 12.0);
            double distance = haversine(centerLat, centerLng, coords[0], coords[1]);

            Video video = new Video(
                    String.format("Medium Video %02d", i),
                    String.format("Video SREDNJE na %.1fkm - MEDIUM activity", distance),
                    List.of("test", "beograd", "medium"),
                    users.get(random.nextInt(users.size())).getId()
            );

            video.setLatitude(coords[0]);
            video.setLongitude(coords[1]);
            video.setLocation(String.format("Beograd Okolina (%.1fkm)", distance));
            video.setIsLocationApproximated(false);
            video.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(14)));

            video = videoRepository.save(video);
            mediumVideos.add(video);
            allVideos.add(video);

            System.out.printf("   Video %02d: %.2fkm @ (%.4f, %.4f)\n",
                    i, distance, coords[0], coords[1]);
        }

        System.out.println("\n GROUP 3: Creating 15 videos in 25-40km (NO activity)");

        for (int i = 36; i <= 50; i++) {
            double[] coords = randomLocationInRing(centerLat, centerLng, 25.0, 40.0);
            double distance = haversine(centerLat, centerLng, coords[0], coords[1]);

            Video video = new Video(
                    String.format("Far Video %02d", i),
                    String.format("Video DALEKO na %.1fkm - NO activity", distance),
                    List.of("test", "beograd", "far"),
                    users.get(random.nextInt(users.size())).getId()
            );

            video.setLatitude(coords[0]);
            video.setLongitude(coords[1]);
            video.setLocation(String.format("Šira Srbija (%.1fkm)", distance));
            video.setIsLocationApproximated(false);
            video.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(30)));

            video = videoRepository.save(video);
            allVideos.add(video);

            System.out.printf("  Video %02d: %.2fkm @ (%.4f, %.4f)\n",
                    i, distance, coords[0], coords[1]);
        }

        System.out.println(" VIDEO DISTRIBUTION:");
        System.out.println("  0-2.5km:  " + closeVideos.size() + " videos (HIGH activity)");
        System.out.println("  6-12km:   " + mediumVideos.size() + " videos (MEDIUM activity)");
        System.out.println("  25-40km:  " + (allVideos.size() - closeVideos.size() - mediumVideos.size()) + " videos (NO activity)");


        System.out.println(" GROUP 1 (0-2.5km): HIGH interaction density");
        int closeLikes = generateInteractionsExact(closeVideos, users, 600, centerLat, centerLng, 2.5, "LIKE");
        int closeViews = generateInteractionsExact(closeVideos, users, 1000, centerLat, centerLng, 2.5, "VIEW");
        int closeComments = generateInteractionsExact(closeVideos, users, 400, centerLat, centerLng, 2.5, "COMMENT");

        System.out.printf(" Generated: %d likes, %d views, %d comments\n\n",
                closeLikes, closeViews, closeComments);


        System.out.println(" GROUP 2 (6-12km): MEDIUM interaction density");
        int mediumLikes = generateInteractionsExact(mediumVideos, users, 150, centerLat, centerLng, 12.0, "LIKE");
        int mediumViews = generateInteractionsExact(mediumVideos, users, 250, centerLat, centerLng, 12.0, "VIEW");
        int mediumComments = generateInteractionsExact(mediumVideos, users, 100, centerLat, centerLng, 12.0, "COMMENT");

        System.out.printf(" Generated: %d likes, %d views, %d comments\n\n",
                mediumLikes, mediumViews, mediumComments);
    }

    /**
     * SCENARIO 2: DISTRIBUIRANE AKTIVNOSTI
     * Pravi videje sirom BALKANA sa raspršenim interakcijama
     */
    @Transactional
    public void generateDistributedScenario() {
        System.out.println(" Generating DISTRIBUTED scenario (Balkan region)");

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


        int videosPerCity = 100 / cities.length;

        for (int cityIdx = 0; cityIdx < cities.length; cityIdx++) {
            double[] city = cities[cityIdx];

            System.out.println("  Creating " + videosPerCity + " videos near city " + cityIdx);

            for (int i = 0; i < videosPerCity; i++) {
                int videoNum = cityIdx * videosPerCity + i + 1;
                Video video = createVideo(
                        "Balkan Video " + videoNum,
                        "Distributed video in city " + cityIdx,
                        users,
                        city[0], city[1],
                        random.nextDouble() * 50.0
                );
                videos.add(videoRepository.save(video));
            }
        }

        System.out.println("   Created " + videos.size() + " distributed videos");

        // Interakcije podeljene po svim gradovima
        for (double[] city : cities) {
            generateInteractions(videos, users, 40, city[0], city[1], 100.0, "LIKE");
            generateInteractions(videos, users, 60, city[0], city[1], 100.0, "VIEW");
            generateInteractions(videos, users, 25, city[0], city[1], 100.0, "COMMENT");
        }

        System.out.println(" DISTRIBUTED scenario generated: " + videos.size() + " videos across " + cities.length + " cities");
    }

    public  double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Zemlja u km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private Video createVideo(String title, String description, List<User> users,
                              double centerLat, double centerLng, double radiusKm) {
        Video video = new Video(
                title,
                description,
                List.of("test", "trending"),
                users.get(random.nextInt(users.size())).getId()
        );

        double[] coords = randomLocationInRing(centerLat, centerLng, 0.0, radiusKm);

        double distance = haversine(centerLat, centerLng, coords[0], coords[1]);
        video.setLatitude(coords[0]);
        video.setLongitude(coords[1]);
        video.setLocation(String.format("Test Location (%.1fkm)", distance));
        video.setIsLocationApproximated(false);
        video.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(30)));

        return video;
    }

    /**
      Generiše tacan broj interakcija za specifične video snimke
     */
    private int generateInteractionsExact(
            List<Video> videos,
            List<User> users,
            int targetCount,
            double centerLat,
            double centerLng,
            double radiusKm,
            String type
    ) {
        int generated = 0;
        int maxAttempts = targetCount * 5;
        Set<String> usedPairs = new HashSet<>();

        for (int attempt = 0; attempt < maxAttempts && generated < targetCount; attempt++) {
            Video video = videos.get(random.nextInt(videos.size()));
            User user = users.get(random.nextInt(users.size()));

            String pairKey = user.getId() + "-" + video.getId();
            if (usedPairs.contains(pairKey) && !type.equals("COMMENT")) {
                continue;
            }

            double[] coords = randomLocationInRing(centerLat, centerLng, 0.0, radiusKm);

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
                            usedPairs.add(pairKey);
                            generated++;
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
                            usedPairs.add(pairKey);
                            generated++;
                        }
                        break;

                    case "COMMENT":
                        Comment comment = new Comment(
                                String.format("Test comment #%d", generated + 1),
                                user,
                                video
                        );
                        comment.setLatitude(coords[0]);
                        comment.setLongitude(coords[1]);
                        comment.setLocationName("Test Location");
                        comment.setIsLocationApproximated(false);
                        commentRepository.save(comment);
                        generated++;
                        break;
                }
            } catch (Exception e) {
                // Skip and continue
            }
        }

        return generated;
    }


    private void generateInteractions(List<Video> videos, List<User> users, int count,
                                      double centerLat, double centerLng, double radiusKm,
                                      String type) {
        generateInteractionsExact(videos, users, count, centerLat, centerLng, radiusKm, type);
    }

    // generise tacku izmedju minKm i maxKm od centra
    private double[] randomLocationInRing(
            double centerLat,
            double centerLng,
            double minKm,
            double maxKm
    ) {
        // Ravnomerna distribucija po povrsini prstena
        double distance = minKm + (maxKm - minKm) * Math.sqrt(random.nextDouble());
        double angle = random.nextDouble() * 2 * Math.PI;

        // Konverzija km u stepene
        double latOffset = (distance / 111.0) * Math.cos(angle);
        double lngOffset = (distance / (111.0 * Math.cos(Math.toRadians(centerLat)))) * Math.sin(angle);

        return new double[]{
                centerLat + latOffset,
                centerLng + lngOffset
        };
    }


    @Transactional
    public void cleanupTestData() {

        commentRepository.deleteAll();
        videoLikeRepository.deleteAll();
        videoViewRepository.deleteAll();


        List<Video> allVideos = videoRepository.findAll();
        List<Video> testVideos = allVideos.stream()
                .filter(v -> v.getTags() != null &&
                        (v.getTags().contains("test") ||
                                v.getTags().contains("beograd") ||
                                v.getTags().contains("balkan")))
                .toList();

        videoRepository.deleteAll(testVideos);
    }
}