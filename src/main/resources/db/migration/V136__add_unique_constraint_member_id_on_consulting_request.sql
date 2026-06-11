ALTER TABLE consulting_request
    ADD CONSTRAINT uk_consulting_request_member_id UNIQUE (member_id);
