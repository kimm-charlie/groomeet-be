CREATE TABLE cash_product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    price BIGINT NOT NULL,
    amount BIGINT NOT NULL,
    discount_rate INT NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL
);

CREATE TABLE cash_transaction_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    cash_usage_type VARCHAR(50) NOT NULL,
    cash_transaction_type VARCHAR(50) NOT NULL,
    amount BIGINT NOT NULL,
    before_balance BIGINT NOT NULL,
    after_balance BIGINT NOT NULL,
    reference_type VARCHAR(100),
    reference_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE member
    ADD COLUMN cash_balance BIGINT NOT NULL DEFAULT 0;


ALTER TABLE director_info
DROP COLUMN cash;