ALTER TABLE service_request
ADD COLUMN is_direct_request BOOLEAN DEFAULT FALSE,
ADD COLUMN direct_requested_member_id BIGINT NULL;

ALTER TABLE chat_room
ADD COLUMN chat_message_id BIGINT NULL;