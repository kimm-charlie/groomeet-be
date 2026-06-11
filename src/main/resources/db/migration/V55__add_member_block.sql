CREATE TABLE member_block
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    blocker_id          BIGINT        NOT NULL,
    blocked_id          BIGINT        NOT NULL,
    created_at         DATETIME      NOT NULL DEFAULT NOW(),
    CONSTRAINT unique_member_block UNIQUE (blocker_id, blocked_id)
);
