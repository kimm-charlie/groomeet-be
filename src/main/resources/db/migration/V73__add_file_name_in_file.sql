ALTER TABLE portfolio_file
    ADD COLUMN file_name VARCHAR(100),
    ADD COLUMN file_size VARCHAR(20);

ALTER TABLE review_file
    ADD COLUMN file_name VARCHAR(100),
    ADD COLUMN file_size VARCHAR(20);

ALTER TABLE service_estimate_file
    ADD COLUMN file_name VARCHAR(100),
    ADD COLUMN file_size VARCHAR(20);

ALTER TABLE service_request_file
    ADD COLUMN file_name VARCHAR(100),
    ADD COLUMN file_size VARCHAR(20);

ALTER TABLE chat_file
    ADD COLUMN file_name VARCHAR(100),
    ADD COLUMN file_size VARCHAR(20);

ALTER TABLE report_file
    ADD COLUMN file_name VARCHAR(100),
    ADD COLUMN file_size VARCHAR(20);