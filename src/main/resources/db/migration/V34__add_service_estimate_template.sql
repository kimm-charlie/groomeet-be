ALTER TABLE director_service_mapping
DROP
COLUMN auto_price,
DROP
COLUMN auto_title,
DROP
COLUMN auto_content;


CREATE TABLE service_estimate_template
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    director_info_id    BIGINT        NOT NULL,
    director_service_id BIGINT        NOT NULL,
    price          BIGINT        NOT NULL,
    title          VARCHAR(100)  NOT NULL,
    content        VARCHAR(1000) NOT NULL,
    active_unique_key   VARCHAR(100)  NOT NULL,
    is_deleted          BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at          DATETIME      NOT NULL DEFAULT NOW()
);

CREATE TABLE service_estimate_template_image
(
    id                           BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id                    BIGINT        NOT NULL,
    service_estimate_template_id BIGINT        NOT NULL,
    origin_url                   VARCHAR(1000) NOT NULL,
    cdn_url                      VARCHAR(1000) NOT NULL,
    sort_order                   INTEGER                DEFAULT 0,
    created_at                   DATETIME      NOT NULL DEFAULT NOW(),
    file_key                     VARCHAR(1000) NOT NULL,
    is_deleted                   BOOLEAN       NOT NULL DEFAULT FALSE
);

