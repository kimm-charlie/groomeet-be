ALTER TABLE member_report RENAME TO report;

CREATE TABLE report_file
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id  BIGINT        NOT NULL,
    report_id  BIGINT NULL,
    origin_url VARCHAR(1000) NOT NULL,
    cdn_url    VARCHAR(1000) NOT NULL,
    sort_order INTEGER                DEFAULT 0,
    created_at DATETIME      NOT NULL DEFAULT NOW(),
    file_key   VARCHAR(1000) NOT NULL,
    is_deleted BOOLEAN       NOT NULL DEFAULT FALSE,
    file_type VARCHAR(20)  NOT NULL
);

ALTER TABLE report
ADD COLUMN description VARCHAR(1000);