INSERT INTO ROLE (id, name) VALUES (1, 'ROLE_USER');
INSERT INTO ROLE (id, name) VALUES (2, 'ROLE_ADMIN');
INSERT INTO ADDRESS (id, street, city, postal_code, country) VALUES (-1, 'Bulevar kralja Aleksandra 73', 'Beograd', '11000', 'Serbia');
INSERT INTO USERS (id, username, password, first_name, last_name, email, address_id, enabled, is_verified, last_password_reset_date) VALUES (-1, 'testuser', '$2a$10$.t.87y6mejycgrHaXKCS..2FOHbgMD0gzWSfnziZ1MsxvAlg88fTO', 'Test', 'User', 'test@example.com', -1, true, true, NOW());
INSERT INTO USER_ROLE (user_id, role_id) VALUES (-1, 1);

-- Roles (već postoje)
INSERT INTO ROLE (id, name) VALUES (1, 'ROLE_USER');
INSERT INTO ROLE (id, name) VALUES (2, 'ROLE_ADMIN');

-- Addresses
INSERT INTO ADDRESS (id, street, city, postal_code, country) VALUES (-1, 'Bulevar kralja Aleksandra 73', 'Beograd', '11000', 'Serbia');
INSERT INTO ADDRESS (id, street, city, postal_code, country) VALUES (-2, 'Kralja Petra 15', 'Novi Sad', '21000', 'Serbia');
INSERT INTO ADDRESS (id, street, city, postal_code, country) VALUES (-3, 'Trg Republike 5', 'Beograd', '11000', 'Serbia');
INSERT INTO ADDRESS (id, street, city, postal_code, country) VALUES (-4, 'Dunavska 22', 'Novi Sad', '21000', 'Serbia');
INSERT INTO ADDRESS (id, street, city, postal_code, country) VALUES (-5, 'Knez Mihailova 10', 'Beograd', '11000', 'Serbia');
INSERT INTO ADDRESS (id, street, city, postal_code, country) VALUES (-6, 'Zmaj Jovina 8', 'Novi Sad', '21000', 'Serbia');
INSERT INTO ADDRESS (id, street, city, postal_code, country) VALUES (-7, 'Makedonska 30', 'Beograd', '11000', 'Serbia');

-- Users
-- User 1 - Regular user
INSERT INTO USERS (id, username, password, first_name, last_name, email, address_id, enabled, is_verified, last_password_reset_date)
VALUES (-1, 'testuser', '$2a$10$.t.87y6mejycgrHaXKCS..2FOHbgMD0gzWSfnziZ1MsxvAlg88fTO', 'Test', 'User', 'test@example.com', -1, true, true, NOW());

-- User 2 - Admin user
INSERT INTO USERS (id, username, password, first_name, last_name, email, address_id, enabled, is_verified, last_password_reset_date)
VALUES (-2, 'admin', '$2a$10$.t.87y6mejycgrHaXKCS..2FOHbgMD0gzWSfnziZ1MsxvAlg88fTO', 'Admin', 'User', 'admin@example.com', -2, true, true, NOW());

-- User 3 - Regular user
INSERT INTO USERS (id, username, password, first_name, last_name, email, address_id, enabled, is_verified, last_password_reset_date)
VALUES (-3, 'marko123', '$2a$10$.t.87y6mejycgrHaXKCS..2FOHbgMD0gzWSfnziZ1MsxvAlg88fTO', 'Marko', 'Marković', 'marko.markovic@example.com', -3, true, true, NOW());

-- User 4 - Regular user
INSERT INTO USERS (id, username, password, first_name, last_name, email, address_id, enabled, is_verified, last_password_reset_date)
VALUES (-4, 'ana_petrovic', '$2a$10$.t.87y6mejycgrHaXKCS..2FOHbgMD0gzWSfnziZ1MsxvAlg88fTO', 'Ana', 'Petrović', 'ana.petrovic@example.com', -4, true, true, NOW());

-- User 5 - Disabled user
INSERT INTO USERS (id, username, password, first_name, last_name, email, address_id, enabled, is_verified, last_password_reset_date)
VALUES (-5, 'disabled_user', '$2a$10$.t.87y6mejycgrHaXKCS..2FOHbgMD0gzWSfnziZ1MsxvAlg88fTO', 'Disabled', 'User', 'disabled@example.com', -5, false, true, NOW());

-- User 6 - Unverified user
INSERT INTO USERS (id, username, password, first_name, last_name, email, address_id, enabled, is_verified, last_password_reset_date)
VALUES (-6, 'unverified_user', '$2a$10$.t.87y6mejycgrHaXKCS..2FOHbgMD0gzWSfnziZ1MsxvAlg88fTO', 'Unverified', 'User', 'unverified@example.com', -6, true, false, NOW());

-- User 7 - Regular user
INSERT INTO USERS (id, username, password, first_name, last_name, email, address_id, enabled, is_verified, last_password_reset_date)
VALUES (-7, 'jovan_jovic', '$2a$10$.t.87y6mejycgrHaXKCS..2FOHbgMD0gzWSfnziZ1MsxvAlg88fTO', 'Jovan', 'Jović', 'jovan.jovic@example.com', -7, true, true, NOW());

-- User roles
INSERT INTO USER_ROLE (user_id, role_id) VALUES (-1, 1);  -- testuser -> ROLE_USER
INSERT INTO USER_ROLE (user_id, role_id) VALUES (-2, 2);  -- admin -> ROLE_ADMIN
INSERT INTO USER_ROLE (user_id, role_id) VALUES (-2, 1);  -- admin -> ROLE_USER (admin ima obe role)
INSERT INTO USER_ROLE (user_id, role_id) VALUES (-3, 1);  -- marko123 -> ROLE_USER
INSERT INTO USER_ROLE (user_id, role_id) VALUES (-4, 1);  -- ana_petrovic -> ROLE_USER
INSERT INTO USER_ROLE (user_id, role_id) VALUES (-5, 1);  -- disabled_user -> ROLE_USER
INSERT INTO USER_ROLE (user_id, role_id) VALUES (-6, 1);  -- unverified_user -> ROLE_USER
INSERT INTO USER_ROLE (user_id, role_id) VALUES (-7, 1);  -- jovan_jovic -> ROLE_USER


INSERT INTO VIDEO_POSTS (id, title, description, thumbnail_path, video_path, created_at, location, user_id, version)
VALUES (-1, 'My Neighbor Totoro', 'Hayao Miyazaki's classic animated film about two sisters who move to the countryside and encounter magical creatures.',
        '/thumbnails/2.jpg', '/videos/2.mp4',
        '2026-01-10 10:30:40', '', -1, 0);