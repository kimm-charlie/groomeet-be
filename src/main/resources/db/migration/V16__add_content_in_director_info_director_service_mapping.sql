ALTER TABLE `director_info_director_service_mapping`
    ADD COLUMN `auto_price` BIGINT,
    ADD COLUMN `auto_title` VARCHAR(100),
    ADD COLUMN `auto_content` VARCHAR(1000);