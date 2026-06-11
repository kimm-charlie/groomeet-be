ALTER TABLE `service_estimate`
    ADD COLUMN `active_unique_key` VARCHAR(100) NULL;
ALTER TABLE `service_estimate`
    ADD CONSTRAINT `unique_service_estimate` UNIQUE (`active_unique_key`);