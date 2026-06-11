-- Create table: location
CREATE TABLE `location`
(
    `id`        BIGINT      NOT NULL AUTO_INCREMENT,
    `parent_id` BIGINT NULL,
    `name`      VARCHAR(30) NOT NULL,
    `type`      VARCHAR(30) NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `director`
(
    `id`             BIGINT   NOT NULL AUTO_INCREMENT,
    `introduce_text` VARCHAR(1000) NULL,
    `store_address`  VARCHAR(255) NULL,
    `created_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE `director_location_mapping`
(
    `id`               BIGINT NOT NULL AUTO_INCREMENT,
    `location_id`      BIGINT NOT NULL,
    `director_info_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `member_location_mapping`
(
    `id`          BIGINT NOT NULL AUTO_INCREMENT,
    `location_id` BIGINT NOT NULL,
    `member_id`   BIGINT NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `services`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `member_id`  BIGINT       NOT NULL,
    `title`      VARCHAR(100) NOT NULL,
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `is_deleted` BOOLEAN      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
);


ALTER TABLE `member`
    ADD COLUMN `is_director` BOOLEAN NOT NULL DEFAULT 0,
ADD COLUMN `director_id` BIGINT NULL;
