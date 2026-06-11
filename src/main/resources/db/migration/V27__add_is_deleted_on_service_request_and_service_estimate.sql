ALTER TABLE service_estimate
    ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;

ALTER TABLE service_request
    ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;