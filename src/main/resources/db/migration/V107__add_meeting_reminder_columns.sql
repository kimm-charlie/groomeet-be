ALTER TABLE meeting
    ADD COLUMN reminder_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN reminder_sent_at DATETIME NULL;

ALTER TABLE meeting
    ADD COLUMN reminder_need_at DATETIME NULL;

UPDATE meeting
SET reminder_need_at = DATE_FORMAT(DATE_SUB(scheduled_at, INTERVAL 1 DAY), '%Y-%m-%d %H:00:00')
WHERE reminder_need_at IS NULL;

ALTER TABLE meeting
    MODIFY reminder_need_at DATETIME NOT NULL;
