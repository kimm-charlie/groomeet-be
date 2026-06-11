CREATE TABLE ai_prompt (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id           BIGINT       NOT NULL,
    director_service_id BIGINT       NULL,
    prompt              VARCHAR(1000) NOT NULL,
    step                VARCHAR(20)  NOT NULL CHECK (step IN ('RECOMMEND', 'GENERATE')),
    ai_content          VARCHAR(1000) NULL,
    is_service_recommend_success BOOLEAN NOT NULL DEFAULT FALSE,
    file_ids            VARCHAR(500) NULL,
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
