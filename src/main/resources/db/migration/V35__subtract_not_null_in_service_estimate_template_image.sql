ALTER TABLE service_estimate_template_image
    MODIFY COLUMN service_estimate_template_id BIGINT;


ALTER TABLE director_info
    CHANGE COLUMN has_frequent_estimate is_estimate_template_exist BOOLEAN NOT NULL DEFAULT FALSE;
