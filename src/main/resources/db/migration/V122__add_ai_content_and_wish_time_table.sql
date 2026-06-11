-- service_request 테이블에 ai_content 컬럼 추가
ALTER TABLE service_request ADD COLUMN ai_content VARCHAR(1000);

-- wish_date 컬럼 타입 변경 (DATETIME -> DATE)
ALTER TABLE service_request MODIFY COLUMN wish_date DATE;

-- service_request_wish_time 테이블 생성
CREATE TABLE service_request_wish_time (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_request_id BIGINT NOT NULL,
    wish_time TIME NOT NULL
);
