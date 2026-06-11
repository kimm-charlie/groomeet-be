CREATE TABLE popular_service_set
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    title      VARCHAR(100) NOT NULL,
    position   VARCHAR(20)  NOT NULL,
    sort_order INTEGER      NOT NULL DEFAULT 0,
    is_deleted BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE popular_service_item
(
    id                     BIGINT        NOT NULL AUTO_INCREMENT,
    popular_service_set_id BIGINT        NOT NULL,
    director_service_id    BIGINT        NOT NULL,
    content                VARCHAR(255)  NOT NULL,
    image_url              VARCHAR(1000) NOT NULL,
    cdn_image_url          VARCHAR(1000) NOT NULL,
    sort_order             INTEGER       NOT NULL DEFAULT 0,
    is_deleted             BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at             DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
