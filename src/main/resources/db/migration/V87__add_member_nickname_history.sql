CREATE TABLE member_nickname_history
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id      BIGINT        NOT NULL,
    from_nickname  VARCHAR(100)  NOT NULL,
    to_nickname    VARCHAR(100)  NOT NULL,
    created_at     DATETIME      NOT NULL DEFAULT NOW()
);
