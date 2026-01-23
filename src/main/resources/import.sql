INSERT INTO ROLE (id, name) VALUES (1, 'ROLE_USER');
INSERT INTO ROLE (id, name) VALUES (2, 'ROLE_ADMIN');

INSERT INTO ADDRESS (id, street, city, postal_code, country, latitude, longitude) VALUES (-1, 'Bulevar kralja Aleksandra 73', 'Beograd', '11000', 'Serbia', 44.7866, 20.4489);
INSERT INTO ADDRESS (id, street, city, postal_code, country, latitude, longitude) VALUES (-2, 'Kralja Petra 15', 'Novi Sad', '21000', 'Serbia', 45.2671, 19.8335);
INSERT INTO ADDRESS (id, street, city, postal_code, country, latitude, longitude) VALUES (-3, 'Trg Republike 5', 'Beograd', '11000', 'Serbia', 44.7866, 20.4489);
INSERT INTO ADDRESS (id, street, city, postal_code, country, latitude, longitude) VALUES (-4, 'Dunavska 22', 'Novi Sad', '21000', 'Serbia', 45.2671, 19.8335);
INSERT INTO ADDRESS (id, street, city, postal_code, country, latitude, longitude) VALUES (-5, 'Knez Mihailova 10', 'Beograd', '11000', 'Serbia', 44.7866, 20.4489);
INSERT INTO ADDRESS (id, street, city, postal_code, country, latitude, longitude) VALUES (-6, 'Zmaj Jovina 8', 'Novi Sad', '21000', 'Serbia', 45.2671, 19.8335);
INSERT INTO ADDRESS (id, street, city, postal_code, country, latitude, longitude) VALUES (-7, 'Makedonska 30', 'Beograd', '11000', 'Serbia', 44.7866, 20.4489);

INSERT INTO USERS (id, username, password, first_name, last_name, email, address_id, enabled, is_verified, last_password_reset_date) VALUES (-1, 'testuser', '$2a$10$.t.87y6mejycgrHaXKCS..2FOHbgMD0gzWSfnziZ1MsxvAlg88fTO', 'Test', 'User', 'test@example.com', -1, true, true, NOW());
INSERT INTO USERS (id, username, password, first_name, last_name, email, address_id, enabled, is_verified, last_password_reset_date) VALUES (-2, 'oscar', '$2a$10$.t.87y6mejycgrHaXKCS..2FOHbgMD0gzWSfnziZ1MsxvAlg88fTO', 'Oscar', 'Brewer', 'admin@example.com', -2, true, true, NOW());
INSERT INTO USERS (id, username, password, first_name, last_name, email, address_id, enabled, is_verified, last_password_reset_date) VALUES (-3, 'the_mapmaker', '$2a$10$.t.87y6mejycgrHaXKCS..2FOHbgMD0gzWSfnziZ1MsxvAlg88fTO', 'Liam', 'Mapmaker', 'marko.markovic@example.com', -3, true, true, NOW());
INSERT INTO USERS (id, username, password, first_name, last_name, email, address_id, enabled, is_verified, last_password_reset_date) VALUES (-4, 'janedoe', '$2a$10$.t.87y6mejycgrHaXKCS..2FOHbgMD0gzWSfnziZ1MsxvAlg88fTO', 'Jane', 'Doe', 'ana.petrovic@example.com', -4, true, true, NOW());
INSERT INTO USERS (id, username, password, first_name, last_name, email, address_id, enabled, is_verified, last_password_reset_date) VALUES (-5, 'mike_fisherman', '$2a$10$.t.87y6mejycgrHaXKCS..2FOHbgMD0gzWSfnziZ1MsxvAlg88fTO', 'Mike', 'Fisherman', 'disabled@example.com', -5, false, true, NOW());
INSERT INTO USERS (id, username, password, first_name, last_name, email, address_id, enabled, is_verified, last_password_reset_date) VALUES (-6, 'david_cooper', '$2a$10$.t.87y6mejycgrHaXKCS..2FOHbgMD0gzWSfnziZ1MsxvAlg88fTO', 'David', 'Cooper', 'unverified@example.com', -6, true, false, NOW());
INSERT INTO USERS (id, username, password, first_name, last_name, email, address_id, enabled, is_verified, last_password_reset_date) VALUES (-7, 'jova', '$2a$10$.t.87y6mejycgrHaXKCS..2FOHbgMD0gzWSfnziZ1MsxvAlg88fTO', 'Jovan', 'Jović', 'jovan.jovic@example.com', -7, true, true, NOW());

INSERT INTO USER_ROLE (user_id, role_id) VALUES (-1, 1);
INSERT INTO USER_ROLE (user_id, role_id) VALUES (-2, 2);
INSERT INTO USER_ROLE (user_id, role_id) VALUES (-2, 1);
INSERT INTO USER_ROLE (user_id, role_id) VALUES (-3, 1);
INSERT INTO USER_ROLE (user_id, role_id) VALUES (-4, 1);
INSERT INTO USER_ROLE (user_id, role_id) VALUES (-5, 1);
INSERT INTO USER_ROLE (user_id, role_id) VALUES (-6, 1);
INSERT INTO USER_ROLE (user_id, role_id) VALUES (-7, 1);


INSERT INTO VIDEO_POSTS (id, title, description, thumbnail_path, video_path, created_at, location, user_id, version, latitude, longitude, is_location_approximated) VALUES (-1, 'Mad Hatter''s Tea Party', 'Alice in Wonderland, famous tea party scene', 'uploads/thumbnails/alice.jpg', 'uploads/videos/alice.mp4', '2026-01-10 10:30:40', 'Novi Sad, Serbia', -1, 0, 45.2671, 19.8335, true);
INSERT INTO VIDEO_POSTS (id, title, description, thumbnail_path, video_path, created_at, location, user_id, version, latitude, longitude, is_location_approximated) VALUES (-2, 'My Neighbor Totoro', 'Hayao Miyazaki''s classic animated film about two sisters who move to the countryside and encounter magical creatures.', 'uploads/thumbnails/totoro.jpg', 'uploads/videos/totoro.mp4', '2026-01-07 18:14:12', 'Beograd, Serbia', -2, 0, 44.7866, 20.4489, true);
INSERT INTO VIDEO_POSTS (id, title, description, thumbnail_path, video_path, created_at, location, user_id, version, latitude, longitude, is_location_approximated) VALUES (-3, 'Lilies', 'Water lilies caught in a rain', 'uploads/thumbnails/ren.jpg', 'uploads/videos/ren.mp4', '2016-12-27 12:58:13', 'Subotica, Serbia', -3, 0, 46.1005, 19.6674, true);
INSERT INTO VIDEO_POSTS (id, title, description, thumbnail_path, video_path, created_at, location, user_id, version, latitude, longitude, is_location_approximated) VALUES (-4, 'Ponyo', 'Sosuke rescues a goldfish trapped in a bottle. The goldfish, who is the daughter of a wizard, transforms herself into a young girl with her father''s magic and falls in love with Sosuke.', 'uploads/thumbnails/ponyo.gif', 'uploads/videos/ponyo.mp4', '2018-08-25 12:58:13', 'Singapore', -2, 0, 1.3521, 103.8198, true);
INSERT INTO VIDEO_POSTS (id, title, description, thumbnail_path, video_path, created_at, location, user_id, version, latitude, longitude, is_location_approximated) VALUES (-5, 'Bratz', 'Iconic opening theme.', 'uploads/thumbnails/bratz.jpg', 'uploads/videos/bratz.mp4', '2021-04-07 12:35:01', 'Novi Sad, Serbia', -7, 0, 45.2671, 19.8335, true);
INSERT INTO VIDEO_POSTS (id, title, description, thumbnail_path, video_path, created_at, location, user_id, version, latitude, longitude, is_location_approximated) VALUES (-6, 'Harry Potter', 'Sorting Hat scene', 'uploads/thumbnails/harry.gif', 'uploads/videos/harry.mp4', '2025-10-10 12:00:13', 'Edinburgh, Scotland', -5, 0, 55.9533, -3.1883, true);
INSERT INTO VIDEO_POSTS (id, title, description, thumbnail_path, video_path, created_at, location, user_id, version, latitude, longitude, is_location_approximated) VALUES (-7, 'yellow garden', 'cat on a sunny day', 'uploads/thumbnails/cat.gif', 'uploads/videos/cat.mp4', '2025-09-10 14:00:13', 'Sombor, Serbia', -3, 0, 45.7733, 19.1122, true);
INSERT INTO VIDEO_POSTS (id, title, description, thumbnail_path, video_path, created_at, location, user_id, version, latitude, longitude, is_location_approximated) VALUES (-8, 'Pixar''s Up', 'Carl, an old widower, goes off on an adventure with the help of Russell, a boy scout, in his flying house to search for Paradise Falls, his wife''s dream destination.', 'uploads/thumbnails/up.gif', 'uploads/videos/up.mp4', '2025-06-03 13:15:01', 'Novi Sad, Serbia', -6, 0, 45.2671, 19.8335, true);


INSERT INTO video_post_tags (post_id, tag) VALUES (-1, 'alice');
INSERT INTO video_post_tags (post_id, tag) VALUES (-1, 'disney');
INSERT INTO video_post_tags (post_id, tag) VALUES (-2, 'ghibli');
INSERT INTO video_post_tags (post_id, tag) VALUES (-3, 'ren');
INSERT INTO video_post_tags (post_id, tag) VALUES (-4, 'ghibli');
INSERT INTO video_post_tags (post_id, tag) VALUES (-5, 'bratz');
INSERT INTO video_post_tags (post_id, tag) VALUES (-6, 'hogwarts');
INSERT INTO video_post_tags (post_id, tag) VALUES (-7, 'cat');
INSERT INTO video_post_tags (post_id, tag) VALUES (-8, 'pixar');
INSERT INTO video_post_tags (post_id, tag) VALUES (-8, 'up');


INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-1, 'Thanks everyone for watching! This scene is my favorite from the movie.', '2026-01-10 10:35:00', -1, -1, 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-2, 'The Mad Hatter is such a quirky character!', '2026-01-10 12:30:00', -2, -1, 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-3, 'Love the colors in this scene!', '2026-01-10 12:45:00', -3, -1, 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-4, 'Classic Disney magic!', '2026-01-10 13:00:00', -4, -1, 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-5, 'This brings back childhood memories.', '2026-01-10 13:15:00', -7, -1, 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-6, 'This is one of the most beautiful animated films I''ve ever seen!', '2026-01-10 11:00:00', -2, -2, 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-7, 'Totoro is such a lovable character. My kids watch this every weekend!', '2026-01-10 11:15:00', -3, -2, 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-8, 'Studio Ghibli never disappoints. A masterpiece!', '2026-01-10 11:30:00', -4, -2, 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-9, 'I love this!!', '2026-01-10 13:00:00', -7, -3, 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-10, 'Lotus flowers are my favorite.', '2026-01-10 13:30:00', -2, -3, 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-11, 'Beautiful cinematography!', '2026-01-10 14:00:00', -4, -3, 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-12, 'Ponyo is adorable! This is my daughter''s favorite movie.', '2026-01-10 15:00:00', -1, -4, 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-13, 'Another Ghibli masterpiece. The animation is stunning!', '2026-01-10 15:15:00', -3, -4, 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-14, 'Sosuke and Ponyo are so cute together!', '2026-01-10 15:30:00', -5, -4, 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-15, 'The ocean scenes are breathtaking.', '2026-01-10 15:45:00', -6, -4, 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-16, 'OMG the Bratz theme song is so nostalgic!', '2026-01-10 16:00:00', -2, -5, 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-17, 'This brings back so many memories from my childhood!', '2026-01-10 16:15:00', -3, -5, 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-18, 'I used to watch Bratz every Saturday morning!', '2026-01-10 16:30:00', -4, -5, 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-19, 'The animation style is iconic.', '2026-01-10 16:45:00', -6, -5, 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-20, 'This song is stuck in my head now!', '2026-01-10 17:00:00', -7, -5, 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-21, 'Gryffindor! That''s where I''d be sorted.', '2026-01-10 17:15:00', -1, -6, 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-22, 'The Sorting Hat ceremony is such an iconic scene.', '2026-01-10 17:30:00', -2, -6, 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-23, 'I love this part of the movie!', '2026-01-10 17:50:00', -3, -6, 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-24, 'Ravenclaw all the way!', '2026-01-10 18:15:00', -4, -6, 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-25, 'The magic in this series is timeless.', '2026-01-10 18:30:00', -7, -6, 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-26, 'What a cute cat!', '2026-01-10 18:45:00', -1, -7, 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-27, 'This reminds me of my garden.', '2026-01-10 19:00:00', -2, -7, 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-28, 'The lighting is perfect!', '2026-01-10 19:15:00', -4, -7, 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-29, 'So peaceful and calming to watch.', '2026-01-10 19:30:00', -5, -7, 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-30, 'I love cats!', '2026-01-10 19:50:00', -6, -7, 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-31, 'Up is one of my all-time favorite Pixar movies!', '2026-01-10 20:00:00', -1, -8, 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-32, 'The opening sequence makes me cry every time.', '2026-01-10 20:15:00', -2, -8, 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-33, 'Carl and Ellie''s love story is beautiful.', '2026-01-10 20:30:00', -3, -8, 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-34, 'Pixar knows how to tell emotional stories.', '2026-01-10 20:45:00', -4, -8, 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id, latitude, longitude, location_name, is_location_approximated) VALUES (-35, 'Russell is such a lovable character!', '2026-01-10 21:00:00', -7, -8, 44.7866, 20.4489, 'Beograd, Serbia', true);
-- VIDEO_VIEWS sa lokacijama (korisnici gledaju sa svojih lokacija)
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-1, -2, -1, '2026-01-10 10:40:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-2, -3, -1, '2026-01-10 10:50:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-3, -4, -1, '2026-01-10 11:00:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-4, -5, -1, '2026-01-10 11:10:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-5, -7, -1, '2026-01-10 11:20:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-6, -2, -2, '2026-01-10 10:55:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-7, -3, -2, '2026-01-10 11:10:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-8, -4, -2, '2026-01-10 11:25:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-9, -6, -2, '2026-01-10 12:00:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-10, -1, -2, '2026-01-10 14:00:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-11, -1, -3, '2026-01-10 12:00:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-12, -2, -3, '2026-01-10 12:30:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-13, -4, -3, '2026-01-10 12:45:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-14, -6, -3, '2026-01-10 13:00:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-15, -7, -3, '2026-01-10 13:45:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-16, -1, -4, '2026-01-10 14:00:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-17, -2, -4, '2026-01-10 14:30:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-18, -3, -4, '2026-01-10 15:00:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-19, -5, -4, '2026-01-10 15:15:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-20, -6, -4, '2026-01-10 15:45:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-21, -1, -5, '2026-01-10 14:15:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-22, -2, -5, '2026-01-10 16:00:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-23, -3, -5, '2026-01-10 16:20:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-24, -4, -5, '2026-01-10 16:35:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-25, -6, -5, '2026-01-10 16:50:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-26, -1, -6, '2026-01-10 14:30:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-27, -2, -6, '2026-01-10 17:05:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-28, -3, -6, '2026-01-10 17:20:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-29, -4, -6, '2026-01-10 17:40:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-30, -7, -6, '2026-01-10 18:05:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-31, -1, -7, '2026-01-10 14:45:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-32, -2, -7, '2026-01-10 18:20:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-33, -4, -7, '2026-01-10 18:35:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-34, -5, -7, '2026-01-10 18:50:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-35, -6, -7, '2026-01-10 19:05:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-36, -1, -8, '2026-01-10 15:00:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-37, -2, -8, '2026-01-10 19:20:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-38, -3, -8, '2026-01-10 19:40:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-39, -4, -8, '2026-01-10 20:05:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at, latitude, longitude, location_name, is_location_approximated) VALUES (-40, -7, -8, '2026-01-10 20:20:00', 44.7866, 20.4489, 'Beograd, Serbia', true);


-- VIDEO_LIKES sa lokacijama (korisnici lajkuju sa svojih lokacija)
-- User -2 (oscar) je iz Novog Sada (45.2671, 19.8335)
-- User -3 (the_mapmaker) je iz Beograda (44.7866, 20.4489)
-- User -4 (janedoe) je iz Novog Sada (45.2671, 19.8335)
-- User -5 (mike_fisherman) je iz Beograda (44.7866, 20.4489)
-- User -7 (jova) je iz Beograda (44.7866, 20.4489)
-- User -1 (testuser) je iz Beograda (44.7866, 20.4489)
-- User -6 (david_cooper) je iz Novog Sada (45.2671, 19.8335)

INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-1, -2, -1, '2026-01-10 10:50:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-2, -3, -1, '2026-01-10 10:55:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-3, -4, -1, '2026-01-10 11:05:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-4, -5, -1, '2026-01-10 11:15:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-5, -7, -1, '2026-01-10 11:25:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-6, -2, -2, '2026-01-10 11:35:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-7, -3, -2, '2026-01-10 11:50:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-8, -6, -2, '2026-01-10 12:05:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-9, -1, -2, '2026-01-10 14:05:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-10, -2, -3, '2026-01-10 12:35:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-11, -4, -3, '2026-01-10 12:50:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-12, -7, -3, '2026-01-10 13:50:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-13, -1, -4, '2026-01-10 14:05:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-14, -2, -4, '2026-01-10 14:35:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-15, -3, -4, '2026-01-10 15:05:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-16, -5, -4, '2026-01-10 15:20:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-17, -1, -5, '2026-01-10 15:00:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-18, -2, -5, '2026-01-10 16:05:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-19, -3, -5, '2026-01-10 16:25:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-20, -4, -5, '2026-01-10 16:40:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-21, -1, -6, '2026-01-10 15:10:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-22, -2, -6, '2026-01-10 17:10:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-23, -3, -6, '2026-01-10 17:25:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-24, -4, -6, '2026-01-10 17:45:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-25, -7, -6, '2026-01-10 18:10:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-26, -1, -7, '2026-01-10 15:20:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-27, -2, -7, '2026-01-10 18:25:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-28, -4, -7, '2026-01-10 18:40:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-29, -5, -7, '2026-01-10 18:55:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-30, -1, -8, '2026-01-10 15:30:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-31, -2, -8, '2026-01-10 19:25:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-32, -3, -8, '2026-01-10 19:45:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-33, -4, -8, '2026-01-10 20:10:00', 45.2671, 19.8335, 'Novi Sad, Serbia', true);
INSERT INTO video_likes (id, user_id, video_id, liked_at, latitude, longitude, location_name, is_location_approximated) VALUES (-34, -7, -8, '2026-01-10 20:25:00', 44.7866, 20.4489, 'Beograd, Serbia', true);
