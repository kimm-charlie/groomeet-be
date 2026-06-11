CREATE TABLE notification
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    receiver_id    BIGINT        NOT NULL,
    type           VARCHAR(50)   NOT NULL,
    title          VARCHAR(200)  NOT NULL,
    content        VARCHAR(1000) NOT NULL,
    reference_id   BIGINT,
    reference_type VARCHAR(50),
    is_read        BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    receiver_type  VARCHAR(20)   NOT NULL
);
