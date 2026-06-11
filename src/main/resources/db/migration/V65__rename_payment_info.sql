ALTER TABLE payment_request RENAME TO meeting;

ALTER TABLE meeting RENAME COLUMN paid_member_id TO member_id;

ALTER TABLE chat_message
RENAME COLUMN payment_request_id TO meeting_id;

ALTER TABLE meeting
DROP COLUMN is_additional;

ALTER TABLE meeting
ADD COLUMN active_unique_key BIGINT;
ALTER TABLE `meeting`
    ADD CONSTRAINT `unique_meeting_for_estimate` UNIQUE (`active_unique_key`);

ALTER TABLE meeting
DROP COLUMN payment_info_id;

DROP TABLE payment_info;