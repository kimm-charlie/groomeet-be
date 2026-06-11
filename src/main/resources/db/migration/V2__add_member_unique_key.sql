-- Add unique constraints to member table
ALTER TABLE `member`
    ADD COLUMN `active_unique_key` VARCHAR(100) NULL;
ALTER TABLE `member`
    ADD CONSTRAINT `unique_member` UNIQUE (`active_unique_key`);
