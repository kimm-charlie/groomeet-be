CREATE TABLE `member`
(
    `id`                         BIGINT        NOT NULL AUTO_INCREMENT,
    `terms_id`                   BIGINT        NOT NULL,
    `image_url`                  VARCHAR(1000) NOT NULL,
    `nickname`                   VARCHAR(100)  NOT NULL,
    `email`                      VARCHAR(100),
    `created_at`                 DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `role`                       VARCHAR(30)   NOT NULL,
    `sign_in_platform`           VARCHAR(10)   NOT NULL,
    `is_withdrawal`              BOOLEAN       NOT NULL,
    `withdrawal_reason`          VARCHAR(20),
    `is_withdrawal_terms_agreed` BOOLEAN       NOT NULL DEFAULT false,
    `withdrawal_at`              DATETIME,
    `identifier`                 VARCHAR(100)  NOT NULL,
    `is_banned`                  BOOLEAN       NOT NULL DEFAULT false,
    `banned_at`                  DATETIME,
    `version`                    VARCHAR(20),
    `version_updated_at`         DATETIME,
    `is_marketing_agreed`        BOOLEAN       NOT NULL,
    `device_type`                VARCHAR(10),
    PRIMARY KEY (`id`)
);

CREATE TABLE `member_terms`
(
    `id`                    BIGINT  NOT NULL AUTO_INCREMENT,
    `service_agreed`        BOOLEAN NOT NULL,
    `privacy_policy_agreed` BOOLEAN NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `refresh_token`
(
    `id`         BIGINT        NOT NULL AUTO_INCREMENT,
    `member_id`  BIGINT        NOT NULL,
    `token`      VARCHAR(1000) NOT NULL,
    `created_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE `popup`
(
    `id`                  BIGINT        NOT NULL AUTO_INCREMENT,
    `content`             VARCHAR(100)  NOT NULL,
    `thumbnail_image_url` VARCHAR(1000) NOT NULL,
    `content_image_url`   VARCHAR(1000) NOT NULL,
    `created_at`          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `start_at`            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `end_at`              DATETIME      NOT NULL,
    `is_deleted`          BOOLEAN       NOT NULL DEFAULT false,
    PRIMARY KEY (`id`)
);

CREATE TABLE `apple_refresh_token`
(
    `id`         BIGINT        NOT NULL AUTO_INCREMENT,
    `member_id`  BIGINT        NOT NULL,
    `token`      VARCHAR(1000) NOT NULL,
    `created_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);