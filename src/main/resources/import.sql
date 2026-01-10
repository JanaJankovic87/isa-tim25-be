INSERT INTO ROLE (id, name) VALUES (1, 'ROLE_USER');
INSERT INTO ROLE (id, name) VALUES (2, 'ROLE_ADMIN');
INSERT INTO ADDRESS (id, street, city, postal_code, country) VALUES (-1, 'Bulevar kralja Aleksandra 73', 'Beograd', '11000', 'Serbia');
INSERT INTO USERS (id, username, password, first_name, last_name, email, address_id, enabled, is_verified, last_password_reset_date) VALUES (-1, 'testuser', '$2a$10$.t.87y6mejycgrHaXKCS..2FOHbgMD0gzWSfnziZ1MsxvAlg88fTO', 'Test', 'User', 'test@example.com', -1, true, true, NOW());
INSERT INTO USER_ROLE (user_id, role_id) VALUES (-1, 1);

INSERT INTO ROLE (id, name) VALUES (1, 'ROLE_USER');
INSERT INTO ROLE (id, name) VALUES (2, 'ROLE_ADMIN');
INSERT INTO ADDRESS (id, street, city, postal_code, country) VALUES (-1, 'Bulevar kralja Aleksandra 73', 'Beograd', '11000', 'Serbia');
INSERT INTO ADDRESS (id, street, city, postal_code, country) VALUES (-2, 'Kralja Petra 15', 'Novi Sad', '21000', 'Serbia');
INSERT INTO ADDRESS (id, street, city, postal_code, country) VALUES (-3, 'Trg Republike 5', 'Beograd', '11000', 'Serbia');
INSERT INTO ADDRESS (id, street, city, postal_code, country) VALUES (-4, 'Dunavska 22', 'Novi Sad', '21000', 'Serbia');
INSERT INTO ADDRESS (id, street, city, postal_code, country) VALUES (-5, 'Knez Mihailova 10', 'Beograd', '11000', 'Serbia');
INSERT INTO ADDRESS (id, street, city, postal_code, country) VALUES (-6, 'Zmaj Jovina 8', 'Novi Sad', '21000', 'Serbia');
INSERT INTO ADDRESS (id, street, city, postal_code, country) VALUES (-7, 'Makedonska 30', 'Beograd', '11000', 'Serbia');
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
INSERT INTO VIDEO_POSTS (id, title, description, thumbnail_path, video_path, created_at, location, user_id, version) VALUES (-1, 'Mad Hatter''s Tea Party ', 'Alice in Wonderland, famous tea party scene', 'uploads/thumbnails/2.jpg', 'uploads/videos/2.mp4', '2026-01-10 10:30:40', '', -1, 0);

INSERT INTO VIDEO_POSTS (id, title, description, thumbnail_path, video_path, created_at, location, user_id, version) VALUES (-2, 'My Neighbor Totoro', 'Hayao Miyazaki''s classic animated film about two sisters who move to the countryside and encounter magical creatures.', 'uploads/thumbnails/3.jpg', 'uploads/videos/3.mp4', '2026-01-7 18:14:12', '', -2, 0);
INSERT INTO VIDEO_POSTS (id, title, description, thumbnail_path, video_path, created_at, location, user_id, version) VALUES (-3, 'Lilies', 'Water lilies caught in a rain', 'uploads/thumbnails/4.jpg', 'uploads/videos/4.mp4', '2016-12-27 12:58:13', '', -3, 0);

INSERT INTO video_post_tags (post_id, tag) VALUES(-1, 'alice');
INSERT INTO video_post_tags (post_id, tag) VALUES(-1, 'disney');
INSERT INTO video_post_tags (post_id, tag) VALUES(-2, 'ghibli');
INSERT INTO video_post_tags (post_id, tag) VALUES(-3, 'ren');

-- Komentari za video -1 (My Neighbor Totoro)
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id) VALUES (-1, 'This is one of the most beautiful animated films I''ve ever seen!', '2026-01-10 11:00:00', -2, -2);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id) VALUES (-2, 'Totoro is such a lovable character. My kids watch this every weekend!', '2026-01-10 11:15:00', -3, -2);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id) VALUES (-3, 'Studio Ghibli never disappoints. A masterpiece!', '2026-01-10 11:30:00', -4, -2);

-- Komentari za video -2
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id) VALUES (-4, 'Iconic!', '2026-01-10 12:00:00', -5, -1);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id) VALUES (-5, 'This is my favorite movie.', '2026-01-10 12:15:00', -6, -1);

-- Komentari za video -3
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id) VALUES (-6, 'I love this!!', '2026-01-10 13:00:00', -7, -3);
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id) VALUES (-7, 'Lotus flowers are my favorite.', '2026-01-10 13:30:00', -2, -3);

-- Dodatni komentar na video -1
INSERT INTO COMMENTS (id, text, created_at, user_id, video_id) VALUES (-8, 'I watch this movie every year. It never gets old!', '2026-01-10 14:00:00', -5, -2);

-- Video pregledi
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at) VALUES (-1, -2, -1, '2026-01-10 10:45:00');
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at) VALUES (-2, -3, -1, '2026-01-10 10:50:00');
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at) VALUES (-3, -4, -1, '2026-01-10 11:00:00');
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at) VALUES (-4, -5, -1, '2026-01-10 11:10:00');
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at) VALUES (-5, -6, -1, '2026-01-10 11:20:00');
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at) VALUES (-6, -2, -2, '2026-01-10 11:30:00');
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at) VALUES (-7, -3, -2, '2026-01-10 11:45:00');
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at) VALUES (-8, -5, -2, '2026-01-10 12:00:00');
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at) VALUES (-9, -2, -3, '2026-01-10 12:30:00');
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at) VALUES (-10, -4, -3, '2026-01-10 12:45:00');
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at) VALUES (-11, -6, -3, '2026-01-10 13:00:00');
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at) VALUES (-12, -7, -1, '2026-01-10 13:15:00');
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at) VALUES (-13, -7, -2, '2026-01-10 13:30:00');
INSERT INTO VIDEO_VIEWS (id, user_id, video_id, viewed_at) VALUES (-14, -7, -3, '2026-01-10 13:45:00');

-- Video lajkovi
INSERT INTO video_likes (id, user_id, video_id, liked_at) VALUES (-1, -2, -1, '2026-01-10 10:50:00');
INSERT INTO video_likes (id, user_id, video_id, liked_at) VALUES (-2, -3, -1, '2026-01-10 10:55:00');
INSERT INTO video_likes (id, user_id, video_id, liked_at) VALUES (-3, -4, -1, '2026-01-10 11:05:00');
INSERT INTO video_likes (id, user_id, video_id, liked_at) VALUES (-4, -5, -1, '2026-01-10 11:15:00');
INSERT INTO video_likes (id, user_id, video_id, liked_at) VALUES (-5, -7, -1, '2026-01-10 11:25:00');
INSERT INTO video_likes (id, user_id, video_id, liked_at) VALUES (-6, -2, -2, '2026-01-10 11:35:00');
INSERT INTO video_likes (id, user_id, video_id, liked_at) VALUES (-7, -3, -2, '2026-01-10 11:50:00');
INSERT INTO video_likes (id, user_id, video_id, liked_at) VALUES (-8, -6, -2, '2026-01-10 12:05:00');
INSERT INTO video_likes (id, user_id, video_id, liked_at) VALUES (-9, -2, -3, '2026-01-10 12:35:00');
INSERT INTO video_likes (id, user_id, video_id, liked_at) VALUES (-10, -4, -3, '2026-01-10 12:50:00');
INSERT INTO video_likes (id, user_id, video_id, liked_at) VALUES (-11, -7, -3, '2026-01-10 13:50:00');