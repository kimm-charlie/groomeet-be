-- SAMPLE → ASPIRATION 리네이밍
UPDATE consulting_request_file SET image_category = 'ASPIRATION' WHERE image_category = 'SAMPLE';

-- recentProcedure 필드 추가
ALTER TABLE consulting_request ADD COLUMN recent_procedure VARCHAR(50) NOT NULL DEFAULT '';

-- 지역 매핑 테이블 생성
CREATE TABLE consulting_request_location_mapping (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    consulting_request_id   BIGINT NOT NULL,
    location_id             BIGINT NOT NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
