CREATE TABLE service_request_image
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id          BIGINT        NOT NULL,
    service_request_id BIGINT NULL,
    origin_url         VARCHAR(1000) NOT NULL,
    cdn_url            VARCHAR(1000) NOT NULL,
    sort_order         INTEGER                DEFAULT 0,
    created_at         DATETIME      NOT NULL DEFAULT NOW(),
    file_key           VARCHAR(1000) NOT NULL,
    is_deleted         BOOLEAN       NOT NULL DEFAULT FALSE
);
