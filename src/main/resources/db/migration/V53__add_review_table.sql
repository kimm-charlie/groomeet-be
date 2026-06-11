CREATE TABLE review
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_estimate_id BIGINT        NOT NULL,
    writer_id           BIGINT        NOT NULL,
    content             VARCHAR(1000) NOT NULL,
    is_deleted          BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at          DATETIME               DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_review_per_estimate UNIQUE (service_estimate_id, writer_id)
);

CREATE TABLE review_image
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id  BIGINT        NOT NULL,
    review_id  BIGINT        NULL,
    origin_url VARCHAR(1000) NOT NULL,
    cdn_url    VARCHAR(1000) NOT NULL,
    sort_order INTEGER                DEFAULT 0,
    created_at DATETIME      NOT NULL DEFAULT NOW(),
    file_key   VARCHAR(1000) NOT NULL,
    is_deleted BOOLEAN       NOT NULL DEFAULT FALSE
);


ALTER TABLE director_info
ADD COLUMN review_count INTEGER NOT NULL DEFAULT 0;