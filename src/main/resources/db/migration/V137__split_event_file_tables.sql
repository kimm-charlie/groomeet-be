CREATE TABLE consulting_request_file (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id               BIGINT       NOT NULL,
    consulting_request_id   BIGINT,
    image_category          VARCHAR(30),
    origin_url              VARCHAR(1000),
    cdn_url                 VARCHAR(1000),
    file_key                VARCHAR(1000),
    sort_order              INT,
    is_deleted              BOOLEAN      NOT NULL DEFAULT FALSE,
    process_status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    file_type               VARCHAR(20)  NOT NULL,
    file_name               VARCHAR(100),
    file_size               VARCHAR(20),
    created_at              DATETIME     NOT NULL
);

CREATE TABLE consulting_sheet_file (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id               BIGINT       NOT NULL,
    consulting_sheet_id     BIGINT,
    origin_url              VARCHAR(1000),
    cdn_url                 VARCHAR(1000),
    file_key                VARCHAR(1000),
    sort_order              INT,
    is_deleted              BOOLEAN      NOT NULL DEFAULT FALSE,
    process_status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    file_type               VARCHAR(20)  NOT NULL,
    file_name               VARCHAR(100),
    file_size               VARCHAR(20),
    created_at              DATETIME     NOT NULL
);

DROP TABLE event_file;
