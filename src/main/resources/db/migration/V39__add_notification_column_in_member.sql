ALTER TABLE member
RENAME COLUMN is_marketing_agreed TO is_marketing_notification_agreed;

ALTER TABLE member
    ADD COLUMN is_chat_notification_agreed BOOLEAN NOT NULL DEFAULT FALSE;