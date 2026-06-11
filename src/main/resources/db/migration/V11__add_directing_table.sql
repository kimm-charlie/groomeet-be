ALTER TABLE `services`
    ADD COLUMN `parent_id` BIGINT NULL,
    RENAME COLUMN `title` TO `name`,
    RENAME TO `director_category`;


ALTER TABLE `director`
    RENAME TO `director_info`;

ALTER TABLE `director_location_mapping`
    RENAME TO `director_info_location_mapping`;

CREATE TABLE `director_info_director_category_mapping`
(
    `id`                    BIGINT   NOT NULL AUTO_INCREMENT,
    `director_info_id`      BIGINT   NOT NULL,
    `directing_category_id` BIGINT   NOT NULL,
    `created_at`            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `is_deleted`            BOOLEAN  NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
);

ALTER TABLE `member`
    RENAME COLUMN `director_id` TO `director_info_id`;