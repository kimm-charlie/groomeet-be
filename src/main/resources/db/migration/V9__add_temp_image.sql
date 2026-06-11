ALTER TABLE `feed_image` DROP COLUMN `type`;

CREATE TABLE `temp_image`
(
    `id`                BIGINT        NOT NULL AUTO_INCREMENT,
    `member_id`         BIGINT        NOT NULL,
    `file_key`          VARCHAR(300)  NOT NULL,
    `origin_url`        VARCHAR(1000) NOT NULL,
    `s3_directory_type` VARCHAR(100)  NOT NULL,
    `created_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `is_deleted`        BOOLEAN       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
);

ALTER TABLE `member`
    ADD COLUMN `cdn_profile_image_url` VARCHAR(1000) NOT NULL,
RENAME COLUMN `image_url` TO `profile_image_url`;
