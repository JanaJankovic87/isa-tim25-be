INSERT INTO ROLE (id, name) VALUES (1, 'ROLE_USER');
INSERT INTO ROLE (id, name) VALUES (2, 'ROLE_ADMIN');

INSERT INTO USERS (id, username, password, first_name, last_name, email, address, enabled, is_verified, last_password_reset_date) VALUES (1, 'testuser', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'Test', 'User', 'test@example.com', 'Test Address', true, true, NOW());

INSERT INTO USER_ROLE (user_id, role_id) VALUES (1, 1);
