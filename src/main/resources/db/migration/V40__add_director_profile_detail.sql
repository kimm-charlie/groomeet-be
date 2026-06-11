CREATE TABLE director_profile_detail
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    content_json     VARCHAR(3000) NULL,
    created_at       DATETIME      NOT NULL DEFAULT NOW()
);

ALTER TABLE director_info
ADD COLUMN director_profile_detail_id BIGINT NOT NULL;