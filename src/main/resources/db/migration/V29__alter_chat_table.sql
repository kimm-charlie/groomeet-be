CREATE TABLE chat_room_service_estimate_mapping
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_estimate_id BIGINT   NOT NULL,
    chat_room_id BIGINT   NOT NULL,
    created_at          DATETIME NOT NULL DEFAULT NOW(),
    is_deleted          BOOLEAN  NOT NULL DEFAULT FALSE,
    active_unique_key VARCHAR(100) NOT NULL,
    CONSTRAINT `unique_member_metadata` UNIQUE (`active_unique_key`)
);

ALTER TABLE chat_room
DROP COLUMN service_estimate_id;

ALTER TABLE chat_room_member
    ADD COLUMN is_chat_room_deleted BOOLEAN NOT NULL DEFAULT FALSE;