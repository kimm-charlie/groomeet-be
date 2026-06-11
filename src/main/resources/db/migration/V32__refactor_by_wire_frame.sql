ALTER TABLE director_service
ADD COLUMN origin_image_url VARCHAR(1000),
ADD COLUMN cdn_image_url VARCHAR(1000),
ADD COLUMN description VARCHAR(500);

ALTER TABLE director_info
ADD COLUMN gender VARCHAR(10) NOT NULL;

ALTER TABLE member
DROP COLUMN gender;

ALTER TABLE director_info
ADD COLUMN is_service_detail_exist BOOLEAN NOT NULL DEFAULT 0,
ADD COLUMN is_portfolio_exist BOOLEAN NOT NULL DEFAULT 0,
ADD COLUMN is_account_verified BOOLEAN NOT NULL DEFAULT 0,
ADD COLUMN has_frequent_estimate BOOLEAN NOT NULL DEFAULT 0;

ALTER TABLE service_request
    MODIFY COLUMN completed_at DATETIME NULL;

ALTER TABLE service_answer_option
    MODIFY content VARCHAR(100) NULL;

ALTER TABLE service_request
    MODIFY COLUMN expired_at DATETIME NOT NULL;

ALTER TABLE portfolio
ADD COLUMN location_id BIGINT NOT NULL,
ADD COLUMN price BIGINT NOT NULL,
ADD COLUMN work_duration VARCHAR(50) NOT NULL,
ADD COLUMN work_year INTEGER NOT NULL;

