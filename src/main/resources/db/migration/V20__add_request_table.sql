CREATE TABLE `request_answer`
(
    `id`                         BIGINT       NOT NULL AUTO_INCREMENT,
    `service_question_option_id` BIGINT       NOT NULL,
    `service_request_id`         BIGINT       NOT NULL,
    `content`                    VARCHAR(100) NOT NULL,
    `created_at`                 DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `is_deleted`                 BOOLEAN      NOT NULL DEFAULT false,
    PRIMARY KEY (`id`)
);

CREATE TABLE `request_location_mapping`
(
    `id`                 BIGINT NOT NULL AUTO_INCREMENT,
    `location_id`        BIGINT NOT NULL,
    `service_request_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `service_question`
(
    `id`                          BIGINT       NOT NULL AUTO_INCREMENT,
    `director_service_id`         BIGINT       NOT NULL,
    `content`                     VARCHAR(100) NOT NULL,
    `question_order`              INTEGER               DEFAULT 0,
    `created_at`                  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `is_deleted`                  BOOLEAN      NOT NULL DEFAULT false,
    `is_multiple_choice_possible` BOOLEAN      NOT NULL DEFAULT false,
    PRIMARY KEY (`id`)
);

CREATE TABLE `service_question_option`
(
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT,
    `service_question_id` BIGINT       NOT NULL,
    `content`             VARCHAR(100),
    `option_type`         VARCHAR(100) NOT NULL,
    `option_order`        INTEGER               DEFAULT 0,
    `created_at`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `is_deleted`          BOOLEAN      NOT NULL DEFAULT false,
    PRIMARY KEY (`id`)
);

CREATE TABLE `service_request`
(
    `id`                  BIGINT   NOT NULL AUTO_INCREMENT,
    `director_service_id` BIGINT   NOT NULL,
    `member_id`           BIGINT   NOT NULL,
    `created_at`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `is_deleted`          BOOLEAN  NOT NULL DEFAULT false,
    `wish_date`           DATETIME NULL,
    `is_ended`            BOOLEAN  NOT NULL DEFAULT false,
    `ended_at`            DATETIME NOT NULL,
    PRIMARY KEY (`id`)
);