ALTER TABLE `director_info_director_service_mapping`
    ADD COLUMN `active_unique_key` VARCHAR(100) NULL;
ALTER TABLE `director_info_director_service_mapping`
    ADD CONSTRAINT `unique_director_info_director_service_mapping` UNIQUE (`active_unique_key`);

