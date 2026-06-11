CREATE TABLE admin
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(100)                        NOT NULL,
    password   VARCHAR(300)                        NOT NULL,
    nickname   VARCHAR(30)                         NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);