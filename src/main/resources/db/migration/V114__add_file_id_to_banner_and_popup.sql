-- Banner에 file_id 추가
ALTER TABLE banner
    ADD COLUMN thumbnail_file_id BIGINT;
ALTER TABLE banner
    ADD COLUMN content_file_id BIGINT;

-- Popup에 file_id 추가
ALTER TABLE popup
    ADD COLUMN thumbnail_file_id BIGINT;
ALTER TABLE popup
    ADD COLUMN content_file_id BIGINT;


ALTER TABLE banner_file
MODIFY COLUMN banner_id BIGINT;

ALTER TABLE popup_file
MODIFY COLUMN popup_id BIGINT;

-- content 관련 필드 삭제
ALTER TABLE popup DROP COLUMN cdn_content_image_url;
ALTER TABLE popup DROP COLUMN content_file_id;

-- link_url 필드 추가 (nullable)
ALTER TABLE popup ADD COLUMN link_url VARCHAR(1000) NULL;

-- type 필드 추가 (NOT NULL, 기본값 MEMBER)
ALTER TABLE popup ADD COLUMN type VARCHAR(20) NOT NULL DEFAULT 'MEMBER';

-- content → title 컬럼명 변경
ALTER TABLE popup RENAME COLUMN content TO title;

-- popup_file 테이블에서 popup_id 컬럼 제거
ALTER TABLE popup_file DROP COLUMN popup_id;

-- banner_file 테이블에서 banner_id 컬럼 제거
ALTER TABLE banner_file DROP COLUMN banner_id;
