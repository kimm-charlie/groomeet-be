CREATE TABLE `feed_image`
(
    `id`         BIGINT        NOT NULL AUTO_INCREMENT,
    `member_id`  BIGINT        NOT NULL,
    `type`       VARCHAR(30) NULL,
    `origin_url` VARCHAR(1000) NOT NULL,
    `cdn_url`    VARCHAR(1000) NOT NULL,
    `created_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `is_deleted` BOOLEAN       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
);
