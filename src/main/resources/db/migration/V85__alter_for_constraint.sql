ALTER TABLE director_info
MODIFY COLUMN introduce_text text;

ALTER TABLE service_request
ADD COLUMN received_estimate_count INTEGER DEFAULT 0 NOT NULL;