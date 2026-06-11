RENAME TABLE service_estimate_template_image TO service_estimate_image;

ALTER TABLE service_estimate_image
    ADD COLUMN service_estimate_id BIGINT NULL;

ALTER TABLE service_estimate_image
    ADD COLUMN image_type VARCHAR(20) NULL;
