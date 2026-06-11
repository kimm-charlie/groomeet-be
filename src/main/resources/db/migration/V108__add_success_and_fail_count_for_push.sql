ALTER TABLE firebase_outbound_log
ADD COLUMN success_count INT DEFAULT 0,
ADD COLUMN failure_count INT DEFAULT 0;