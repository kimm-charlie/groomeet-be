CREATE TABLE consulting_request (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id        BIGINT       NOT NULL,
    uses_hair_product          BOOLEAN NOT NULL,
    prefers_exposed_forehead   BOOLEAN NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    director_info_id BIGINT,
    reserved_at      DATETIME,
    created_at       DATETIME     NOT NULL,
    is_deleted       BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE consulting_sheet (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    consulting_request_id   BIGINT       NOT NULL,
    director_info_id        BIGINT       NOT NULL,
    content                 TEXT,
    status                  VARCHAR(20)  NOT NULL,
    approved_at             DATETIME,
    created_at              DATETIME     NOT NULL,
    is_deleted              BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE event_file (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id               BIGINT       NOT NULL,
    consulting_request_id   BIGINT,
    consulting_sheet_id     BIGINT,
    event_file_type         VARCHAR(30)  NOT NULL,
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

-- consulting_sheet: request당 활성 sheet 1개만 허용 (애플리케이션에서 값 세팅)
ALTER TABLE consulting_sheet
    ADD COLUMN active_request_id BIGINT;

ALTER TABLE consulting_sheet
    ADD CONSTRAINT uk_consulting_sheet_active_request UNIQUE (active_request_id);
