CREATE TABLE popular_service_item_file
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id       BIGINT        NOT NULL,
    origin_url     VARCHAR(1000) NOT NULL,
    cdn_url        VARCHAR(1000) NOT NULL,
    file_key       VARCHAR(1000) NOT NULL,
    is_deleted     BOOLEAN       NOT NULL DEFAULT FALSE,
    process_status VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    created_at     DATETIME      NOT NULL DEFAULT NOW(),
    file_type      VARCHAR(20)   NOT NULL,
    file_name      VARCHAR(100),
    file_size      VARCHAR(20),
    directory_type VARCHAR(50)   NOT NULL
);

ALTER TABLE popular_service_item
    ADD COLUMN file_id BIGINT;

ALTER TABLE popular_service_item
    DROP COLUMN image_url;
