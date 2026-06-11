ALTER TABLE director_service
    ADD COLUMN popular_image_url VARCHAR(1000),
    ADD COLUMN popular_cdn_image_url VARCHAR(1000);

CREATE TABLE popular_director_service
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    director_service_id BIGINT       NOT NULL,
    sort_order          INTEGER      NOT NULL DEFAULT 0,
    is_deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
