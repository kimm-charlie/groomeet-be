-- service_request_wish_time.wish_time: TIME → DATETIME (날짜+시간 통합)
ALTER TABLE service_request_wish_time ADD COLUMN wish_date_time DATETIME;

UPDATE service_request_wish_time swt
INNER JOIN service_request sr ON swt.service_request_id = sr.id
SET swt.wish_date_time = TIMESTAMP(sr.wish_date, swt.wish_time)
WHERE sr.wish_date IS NOT NULL;

-- wish_date가 null인 레거시 데이터: wish_time만으로 오늘 날짜 기준 변환
UPDATE service_request_wish_time swt
INNER JOIN service_request sr ON swt.service_request_id = sr.id
SET swt.wish_date_time = TIMESTAMP(CURDATE(), swt.wish_time)
WHERE sr.wish_date IS NULL AND swt.wish_date_time IS NULL;

ALTER TABLE service_request_wish_time DROP COLUMN wish_time;
ALTER TABLE service_request_wish_time CHANGE COLUMN wish_date_time wish_time DATETIME NOT NULL;

-- service_estimate.scheduled_at: 컬럼 추가
ALTER TABLE service_estimate ADD COLUMN scheduled_at DATETIME;
