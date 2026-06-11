

ALTER TABLE banner_file
ADD COLUMN admin_id BIGINT NOT NULL;

ALTER TABLE banner_file
    ADD COLUMN process_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

ALTER TABLE banner_file
    ADD COLUMN directory_type VARCHAR(30) NOT NULL;

ALTER TABLE banner_file
DROP COLUMN sort_order;

ALTER TABLE popup_file
    ADD COLUMN admin_id BIGINT NOT NULL;

ALTER TABLE popup_file
    ADD COLUMN process_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

ALTER TABLE popup_file
    ADD COLUMN directory_type VARCHAR(30) NOT NULL;

ALTER TABLE popup_file
DROP COLUMN sort_order;

ALTER TABLE banner
DROP COLUMN content_image_url,
DROP COLUMN thumbnail_image_url;

ALTER TABLE popup
DROP COLUMN content_image_url,
DROP COLUMN thumbnail_image_url;
