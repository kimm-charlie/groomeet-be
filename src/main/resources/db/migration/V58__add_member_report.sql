CREATE TABLE member_report
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    reporter_id BIGINT      NOT NULL,
    reported_id BIGINT      NOT NULL,
    report_Type VARCHAR(30) NOT NULL,
    reason      VARCHAR(50) NOT NULL,
    created_at  DATETIME    NOT NULL DEFAULT NOW()
);
