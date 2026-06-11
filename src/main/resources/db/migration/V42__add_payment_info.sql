CREATE TABLE payment_info
(
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id                VARCHAR(100) NOT NULL,
    total_amount            BIGINT       NOT NULL,
    balance_amount          BIGINT       NOT NULL,
    is_partial_cancelable   BOOLEAN      NOT NULL,
    method_code             INT NULL,
    approved_at             DATETIME NULL,
    canceled_at             DATETIME NULL,
    card_company            VARCHAR(20) NULL,
    easy_pay_company        VARCHAR(30) NULL,
    bank                    VARCHAR(20) NULL,
    installment_plan_months INT NULL,
    created_at              DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE receipt
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_estimate_id BIGINT        NOT NULL,
    member_id           BIGINT        NOT NULL,
    payment_info_id     BIGINT NULL,
    content             VARCHAR(1000) NOT NULL,
    price               BIGINT        NOT NULL,
    scheduled_at        DATETIME      NOT NULL,
    created_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at        DATETIME NULL,
    canceled_at         DATETIME NULL,
    refunded_at         DATETIME NULL,
    status              VARCHAR(20)   NOT NULL
);

ALTER TABLE chat_message
DROP
COLUMN chat_message_payment_id;

ALTER TABLE chat_message
    ADD COLUMN receipt_id BIGINT NULL,
ADD is_read_by_opponent BOOLEAN NOT NULL DEFAULT 0;