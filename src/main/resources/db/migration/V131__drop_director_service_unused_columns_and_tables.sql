-- 1. director_service 테이블에서 불필요한 컬럼 삭제 (FK 먼저 제거)
ALTER TABLE director_service DROP COLUMN description_file_id;
ALTER TABLE director_service DROP COLUMN popular_file_id;
ALTER TABLE director_service DROP COLUMN cdn_image_url;
ALTER TABLE director_service DROP COLUMN description;
ALTER TABLE director_service DROP COLUMN popular_cdn_image_url;
ALTER TABLE director_service DROP COLUMN popular_description;
ALTER TABLE director_service DROP COLUMN show_description;

-- 2. director_service_file 테이블 삭제
DROP TABLE IF EXISTS director_service_file;

-- 3. popular_director_service 테이블 삭제
DROP TABLE IF EXISTS popular_director_service;
