RENAME TABLE popular_service_set TO custom_banner_set;
RENAME TABLE popular_service_item TO custom_banner_item;
RENAME TABLE popular_service_item_file TO custom_banner_item_file;

ALTER TABLE custom_banner_item
    CHANGE COLUMN popular_service_set_id custom_banner_set_id BIGINT NOT NULL;
