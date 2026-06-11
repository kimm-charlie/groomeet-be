ALTER TABLE chat_image
    MODIFY chat_message_id BIGINT NULL,
    ADD COLUMN member_id BIGINT NOT NULL;

ALTER TABLE portfolio_image
    ADD COLUMN sort_order INTEGER NOT NULL DEFAULT 0;