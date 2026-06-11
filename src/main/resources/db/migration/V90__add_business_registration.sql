CREATE TABLE business_registration_file
(
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_registration_id BIGINT NULL,
    member_id                BIGINT        NOT NULL,
    origin_url               VARCHAR(1000) NOT NULL,
    cdn_url                  VARCHAR(1000) NOT NULL,
    sort_order               INTEGER                DEFAULT 0,
    created_at               DATETIME      NOT NULL DEFAULT NOW(),
    file_key                 VARCHAR(1000) NOT NULL,
    is_deleted               BOOLEAN       NOT NULL DEFAULT FALSE,
    file_type                VARCHAR(20)   NOT NULL,
    file_name                VARCHAR(100),
    file_size                VARCHAR(20)
);


CREATE TABLE business_registration
(
    id                           BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id                    BIGINT       NOT NULL UNIQUE,
    business_registration_number VARCHAR(200) NOT NULL,
    resident_registration_number VARCHAR(200) NOT NULL,
    created_at                   DATETIME     NOT NULL DEFAULT NOW()
);