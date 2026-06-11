ALTER TABLE `chat_message`
    ADD COLUMN `is_visible_to_opponent` BOOLEAN NOT NULL DEFAULT TRUE;