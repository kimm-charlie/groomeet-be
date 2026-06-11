-- chat_file의 is_deleted에 NOT NULL 제약조건 추가
-- 먼저 기존 null 값을 false로 업데이트 후 NOT NULL 적용
UPDATE chat_file SET is_deleted = FALSE WHERE is_deleted IS NULL;
ALTER TABLE chat_file MODIFY COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
