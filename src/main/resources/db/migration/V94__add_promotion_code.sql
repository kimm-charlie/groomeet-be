CREATE TABLE promotion_code
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    code         VARCHAR(100) NOT NULL,
    used_count   INTEGER      NOT NULL DEFAULT 0,
    created_at   DATETIME     NOT NULL DEFAULT NOW(),
    usage_type   VARCHAR(20)  NOT NULL,
    start_at     DATETIME     NOT NULL,
    end_at       DATETIME     NOT NULL,
    is_deleted   TINYINT(1)   NOT NULL DEFAULT 0,
    description  VARCHAR(30),
    limit_count  INTEGER      NOT NULL DEFAULT 0
);

CREATE TABLE code_usage_history
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    inviter_member_id  BIGINT      NULL,
    promotion_code_id  BIGINT      NULL,
    invitee_member_id  BIGINT      NOT NULL,
    created_at         DATETIME    NOT NULL DEFAULT NOW()
);

drop table if exists member_referral_code_usage;