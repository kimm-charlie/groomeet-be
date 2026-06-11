ALTER TABLE portfolio_image ADD COLUMN file_type VARCHAR(20) NOT NULL;
ALTER TABLE service_estimate_image ADD COLUMN file_type VARCHAR(20) NOT NULL;
ALTER TABLE service_estimate_image RENAME COLUMN image_type TO estimate_type;
ALTER TABLE review_image ADD COLUMN file_type VARCHAR(20) NOT NULL;
ALTER TABLE chat_image ADD COLUMN file_type VARCHAR(20) NOT NULL;
ALTER TABLE service_request_image ADD COLUMN file_type VARCHAR(20) NOT NULL;

ALTER TABLE portfolio_image RENAME TO portfolio_file;
ALTER TABLE service_estimate_image RENAME TO service_estimate_file;
ALTER TABLE review_image RENAME TO review_file;
ALTER TABLE chat_image RENAME TO chat_file;
ALTER TABLE service_request_image RENAME TO service_request_file;