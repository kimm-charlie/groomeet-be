CREATE TABLE member_director_favorite
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id        BIGINT NOT NULL,
    target_member_id BIGINT NOT NULL,
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_member_director_favorite (member_id, target_member_id)
);
