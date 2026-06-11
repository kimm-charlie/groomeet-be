ALTER TABLE message_outbound_log
    MODIFY receiver_id BIGINT NULL,
    ADD COLUMN target_count INT NOT NULL DEFAULT 1 AFTER receiver_id,
    DROP COLUMN is_success,
    DROP COLUMN dispatch_id,
    DROP COLUMN failed_message;
