CREATE TABLE story
(
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    title                   VARCHAR(100)  NOT NULL,
    thumbnail_image_url     VARCHAR(1000) NOT NULL,
    content_image_url       VARCHAR(1000) NOT NULL,
    thumbnail_image_cdn_url VARCHAR(1000) NOT NULL,
    content_image_cdn_url   VARCHAR(1000) NOT NULL,
    is_deleted              TINYINT(1) NOT NULL DEFAULT FALSE,
    sort_order              INT           NOT NULL DEFAULT 0,
    created_at              DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
)