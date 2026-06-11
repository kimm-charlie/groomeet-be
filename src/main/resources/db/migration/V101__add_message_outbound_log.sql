CREATE TABLE message_outbound_log
(
    id               BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    channel          VARCHAR(20) NOT NULL,
    push_event_type  VARCHAR(100),
    kakao_event_type VARCHAR(100),
    receiver_type    VARCHAR(50) NOT NULL,
    receiver_id      BIGINT      NOT NULL,
    sender_type    VARCHAR(50) NOT NULL,
    sender_id        VARCHAR(50),
    reference_type   VARCHAR(50),
    reference_id     BIGINT,
    campaign_key     BIGINT      NOT NULL,
    is_success       BOOLEAN     NOT NULL,
    sent_at          DATETIME    NOT NULL,
    dispatch_id      VARCHAR(50) NULL,
    failed_message   VARCHAR(255) NULL
);

