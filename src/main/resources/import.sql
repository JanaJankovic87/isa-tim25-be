INSERT INTO ROLE (id, name) VALUES (1, 'ROLE_USER');
INSERT INTO ROLE (id, name) VALUES (2, 'ROLE_ADMIN');
INSERT INTO ADDRESS (id, street, city, postal_code, country) VALUES (-1, 'Bulevar kralja Aleksandra 73', 'Beograd', '11000', 'Serbia');
INSERT INTO USERS (id, username, password, first_name, last_name, email, address_id, enabled, is_verified, last_password_reset_date) VALUES (-1, 'testuser', '$2a$10$.t.87y6mejycgrHaXKCS..2FOHbgMD0gzWSfnziZ1MsxvAlg88fTO', 'Test', 'User', 'test@example.com', -1, true, true, NOW());
INSERT INTO USER_ROLE (user_id, role_id) VALUES (-1, 1);