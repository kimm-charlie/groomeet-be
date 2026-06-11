ALTER TABLE portfolio_file
    ADD COLUMN process_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';
ALTER TABLE service_estimate_file
    ADD COLUMN process_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';
ALTER TABLE review_file
    ADD COLUMN process_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';
ALTER TABLE chat_file
    ADD COLUMN process_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';
ALTER TABLE service_request_file
    ADD COLUMN process_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';
ALTER TABLE report_file
    ADD COLUMN process_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';
ALTER TABLE director_profile_detail_file
    ADD COLUMN process_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';
ALTER TABLE profile_file
    ADD COLUMN process_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';
ALTER TABLE business_registration_file
    ADD COLUMN process_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';
