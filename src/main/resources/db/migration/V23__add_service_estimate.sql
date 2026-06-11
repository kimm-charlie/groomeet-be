CREATE TABLE `service_estimate`
(
    `id`                 BIGINT        NOT NULL AUTO_INCREMENT,
    `director_info_id`   BIGINT        NOT NULL,
    `service_request_id` BIGINT        NOT NULL,
    `title`              VARCHAR(100)  NOT NULL,
    `price`              BIGINT        NOT NULL,
    `content`            VARCHAR(1000) NOT NULL,
    `created_at`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `is_ongoing`         BOOLEAN       NOT NULL DEFAULT false,
    `ongoing_at`         DATETIME NULL,
    `is_canceled`        BOOLEAN       NOT NULL DEFAULT false,
    `canceled_at`        DATETIME NULL,
    `is_completed`       BOOLEAN       NOT NULL DEFAULT false,
    `completed_at`       DATETIME NULL,
    PRIMARY KEY (`id`)
);


ALTER TABLE `service_request`
    ADD COLUMN `is_ongoing` BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN `ongoing_at` DATETIME NULL,
    ADD COLUMN `is_expired` BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN `expired_at` DATETIME NULL,
    ADD COLUMN `is_canceled` BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN `canceled_at` DATETIME NULL,
DROP
COLUMN `is_deleted`;

ALTER TABLE `service_request`
    RENAME COLUMN `is_ended` TO `is_completed`,
    RENAME COLUMN `ended_at` TO `completed_at`;