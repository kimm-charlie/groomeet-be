CREATE TABLE banner
(
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    title                   VARCHAR(100)  NOT NULL,
    content_image_url       VARCHAR(1000),
    content_image_cdn_url   VARCHAR(1000),
    thumbnail_image_url     VARCHAR(1000) NOT NULL,
    thumbnail_image_cdn_url VARCHAR(1000) NOT NULL,
    is_deleted              TINYINT(1) NOT NULL DEFAULT FALSE,
    created_at              DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    start_at                DATETIME      NOT NULL,
    end_at                  DATETIME          NOT NULL,
    sort_order              INT           NOT NULL DEFAULT 0,
    is_web_view_banner      TINYINT(1) NOT NULL DEFAULT FALSE,
    web_view_url            VARCHAR(1000)
);
