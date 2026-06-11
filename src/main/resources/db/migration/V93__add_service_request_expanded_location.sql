ALTER TABLE service_request
    ADD COLUMN is_location_expanded BOOLEAN NOT NULL DEFAULT FALSE,
	ADD COLUMN location_expanded_at DATETIME NULL,
	ADD COLUMN expanded_location_id BIGINT NULL;
