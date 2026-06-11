-- 테이블명 변경
RENAME TABLE receipt TO payment_request;

-- 컬럼명 변경
ALTER TABLE payment_request
    RENAME COLUMN member_id TO paid_member_id;

ALTER TABLE chat_message
    RENAME COLUMN receipt_id TO payment_request_id;