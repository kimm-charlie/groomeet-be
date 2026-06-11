CREATE TABLE popular_portfolio
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    portfolio_id BIGINT   NOT NULL,
    is_deleted   BOOLEAN  NOT NULL DEFAULT FALSE,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_popular_portfolio_portfolio
        FOREIGN KEY (portfolio_id) REFERENCES portfolio (id),
    CONSTRAINT uk_popular_portfolio_portfolio_id
        UNIQUE (portfolio_id)
);
