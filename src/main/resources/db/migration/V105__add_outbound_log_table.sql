DROP TABLE IF EXISTS message_outbound_log;

CREATE TABLE firebase_outbound_log
(
    id                  BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    receiver_id         BIGINT       NOT NULL,
    receiver_type       VARCHAR(50)  NOT NULL,
    sender_id           BIGINT       NOT NULL,
    sender_type         VARCHAR(150) NOT NULL,
    reference_type      VARCHAR(30),
    reference_id        BIGINT,
    send_at             TIMESTAMP    NOT NULL,
    target_count        INTEGER      NOT NULL DEFAULT 1,
    firebase_event_type VARCHAR(100)
);

CREATE TABLE hackle_outbound_log
(
    id             BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    receiver_id    BIGINT       NOT NULL,
    receiver_type  VARCHAR(50)  NOT NULL,
    sender_id      BIGINT       NOT NULL,
    sender_type    VARCHAR(150) NOT NULL,
    reference_type VARCHAR(30),
    reference_id   BIGINT,
    send_at        TIMESTAMP    NOT NULL,
    target_count   INTEGER      NOT NULL DEFAULT 1,
    campaign_spec  VARCHAR(100)
);

CREATE TABLE fcm_token
(
    id           BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    member_id    BIGINT,
    token        VARCHAR(1000) NOT NULL,
    used_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    failed_count INTEGER       NOT NULL DEFAULT 0,
    is_deleted   TINYINT(1)    NOT NULL DEFAULT 0
);