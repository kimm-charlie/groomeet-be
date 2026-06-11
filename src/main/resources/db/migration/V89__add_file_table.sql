CREATE TABLE profile_file
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id  BIGINT        NOT NULL,
    origin_url VARCHAR(1000) NOT NULL,
    cdn_url    VARCHAR(1000) NOT NULL,
    sort_order INTEGER                DEFAULT 0,
    created_at DATETIME      NOT NULL DEFAULT NOW(),
    file_key   VARCHAR(1000) NOT NULL,
    is_deleted BOOLEAN       NOT NULL DEFAULT FALSE,
    file_type  VARCHAR(20)   NOT NULL,
    file_name  VARCHAR(100),
    file_size  VARCHAR(20)
);

CREATE TABLE banner_file
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    banner_id  BIGINT        NOT NULL,
    origin_url VARCHAR(1000) NOT NULL,
    cdn_url    VARCHAR(1000) NOT NULL,
    sort_order INTEGER                DEFAULT 0,
    created_at DATETIME      NOT NULL DEFAULT NOW(),
    file_key   VARCHAR(1000) NOT NULL,
    is_deleted BOOLEAN       NOT NULL DEFAULT FALSE,
    file_type  VARCHAR(20)   NOT NULL,
    file_name  VARCHAR(100),
    file_size  VARCHAR(20)
);

CREATE TABLE popup_file
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    popup_id   BIGINT        NOT NULL,
    origin_url VARCHAR(1000) NOT NULL,
    cdn_url    VARCHAR(1000) NOT NULL,
    sort_order INTEGER                DEFAULT 0,
    created_at DATETIME      NOT NULL DEFAULT NOW(),
    file_key   VARCHAR(1000) NOT NULL,
    is_deleted BOOLEAN       NOT NULL DEFAULT FALSE,
    file_type  VARCHAR(20)   NOT NULL,
    file_name  VARCHAR(100),
    file_size  VARCHAR(20)
);

ALTER TABLE member
drop
column profile_image_url;