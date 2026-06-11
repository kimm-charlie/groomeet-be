ALTER TABLE service_estimate
DROP COLUMN is_completed;

ALTER TABLE service_estimate
DROP COLUMN completed_at;

ALTER TABLE service_estimate
MODIFY COLUMN status VARCHAR(50) NOT NULL;

ALTER TABLE service_request
MODIFY COLUMN status VARCHAR(50) NOT NULL;