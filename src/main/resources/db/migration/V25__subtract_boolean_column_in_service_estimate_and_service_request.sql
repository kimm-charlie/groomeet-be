ALTER TABLE service_estimate
DROP
COLUMN is_ongoing,
DROP
COLUMN is_completed,
DROP
COLUMN is_canceled,
ADD COLUMN expired_at DATETIME NULL,
ADD COLUMN status VARCHAR(10) NOT NULL DEFAULT 'PENDING';

ALTER TABLE service_request
DROP
COLUMN is_ongoing,
DROP
COLUMN is_completed,
DROP
COLUMN is_canceled,
DROP
COLUMN is_expired,
ADD COLUMN status VARCHAR(10) NOT NULL DEFAULT 'PENDING';