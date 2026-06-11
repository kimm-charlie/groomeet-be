CREATE TABLE chat_room
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_estimate_id BIGINT   NOT NULL,
    created_at          DATETIME NOT NULL DEFAULT NOW(),
    is_deleted          BOOLEAN  NOT NULL DEFAULT FALSE
);

CREATE TABLE chat_image
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat_message_id BIGINT        NOT NULL,
    origin_url      VARCHAR(1000) NOT NULL,
    cdn_url         VARCHAR(1000) NOT NULL,
    file_key        VARCHAR(255)  NOT NULL,
    sort_order      INT           NOT NULL DEFAULT 0,
    created_at      DATETIME      NOT NULL DEFAULT NOW()
);

CREATE TABLE chat_message
(
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat_room_id            BIGINT      NOT NULL,
    chat_message_payment_id BIGINT NULL,
    chat_room_member_id     BIGINT      NOT NULL,
    message_type            VARCHAR(10) NOT NULL,
    content                 VARCHAR(1000) NULL,
    is_deleted              BOOLEAN     NOT NULL DEFAULT FALSE,
    send_at                 DATETIME    NOT NULL DEFAULT NOW()
);

CREATE TABLE chat_room_member
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat_room_id         BIGINT   NOT NULL,
    member_id            BIGINT   NOT NULL,
    last_read_message_id BIGINT NULL,
    is_active            BOOLEAN  NOT NULL DEFAULT TRUE,
    is_director          BOOLEAN  NOT NULL DEFAULT FALSE,
    left_at              DATETIME NULL,
    created_at           DATETIME NOT NULL DEFAULT NOW()
);

CREATE TABLE chat_message_payment
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    price      BIGINT      NOT NULL,
    status     VARCHAR(10) NOT NULL,
    created_at DATETIME    NOT NULL DEFAULT NOW()
);