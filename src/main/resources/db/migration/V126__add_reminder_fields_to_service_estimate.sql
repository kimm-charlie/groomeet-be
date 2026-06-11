ALTER TABLE service_estimate ADD COLUMN reminder_need_at DATETIME NULL;
ALTER TABLE service_estimate ADD COLUMN reminder_status VARCHAR(20) NULL;
ALTER TABLE service_estimate ADD COLUMN reminder_sent_at DATETIME NULL;


ALTER TABLE service_request DROP COLUMN wish_date;
