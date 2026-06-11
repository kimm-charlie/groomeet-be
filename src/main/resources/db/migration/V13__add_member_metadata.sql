CREATE TABLE `member_metadata`
(
    `id`                BIGINT       NOT NULL AUTO_INCREMENT,
    `member_id`         BIGINT       NOT NULL,
    `device_type`       VARCHAR(30) NULL,
    `version`           VARCHAR(30) NULL,
    `last_updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `active_unique_key` VARCHAR(100) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `unique_member_metadata` UNIQUE (`active_unique_key`)
);


ALTER TABLE `member`
DROP
COLUMN `device_type`,
DROP
COLUMN `version`,
DROP
COLUMN `version_updated_at`;
