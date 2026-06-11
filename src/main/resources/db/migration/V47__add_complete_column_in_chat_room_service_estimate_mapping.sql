ALTER TABLE service_estimate
ADD COLUMN is_completed BOOLEAN DEFAULT FALSE,
ADD COLUMN director_completed_at DATETIME NULL,
ADD COLUMN member_completed_at DATETIME NULL;