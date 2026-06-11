CREATE TABLE member_location_mapping
(
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT,
    `location_id` BIGINT NOT NULL,
    `member_id`   BIGINT NOT NULL,
    `active_unique_key` VARCHAR(100) NOT NULL,
    CONSTRAINT `unique_member_location_mapping` UNIQUE (`active_unique_key`)
);