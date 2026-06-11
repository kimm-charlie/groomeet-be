CREATE TABLE prompt_room (
    id                           BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id                    BIGINT   NOT NULL,
    director_service_id          BIGINT,
    turn_count                   INT      NOT NULL DEFAULT 0,
    is_service_recommend_success BOOLEAN,
    created_at                   DATETIME NOT NULL
);

CREATE TABLE prompt_message (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    prompt_room_id BIGINT      NOT NULL,
    role           VARCHAR(20) NOT NULL,
    content        TEXT        NOT NULL,
    file_ids       VARCHAR(500),
    created_at     DATETIME    NOT NULL
);

CREATE INDEX idx_prompt_room_member_id ON prompt_room (member_id);
CREATE INDEX idx_prompt_message_room_id ON prompt_message (prompt_room_id);
