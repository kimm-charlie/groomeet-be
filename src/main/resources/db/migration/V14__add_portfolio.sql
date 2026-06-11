drop table `feed_image`;
drop table `temp_image`;

CREATE TABLE `portfolio`
(
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT,
    `director_service_id` BIGINT       NOT NULL,
    `director_info_id`    BIGINT       NOT NULL,
    `title`               VARCHAR(100) NOT NULL,
    `content`             VARCHAR(1000) NULL,
    `created_at`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `is_deleted`          BOOLEAN      NOT NULL DEFAULT false,
    PRIMARY KEY (`id`)
);

CREATE TABLE `portfolio_image`
(
    `id`                 BIGINT        NOT NULL AUTO_INCREMENT,
    `member_id`          BIGINT        NOT NULL,
    `portfolio_id`       BIGINT,
    `origin_url`         VARCHAR(1000) NOT NULL,
    `cdn_url`            VARCHAR(1000) NOT NULL,
    `created_at`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `is_deleted`         BOOLEAN       NOT NULL DEFAULT 0,
    `is_thumbnail_image` BOOLEAN       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
);

ALTER TABLE `portfolio_image`
    ADD COLUMN `file_key` VARCHAR(1000) NOT NULL;
