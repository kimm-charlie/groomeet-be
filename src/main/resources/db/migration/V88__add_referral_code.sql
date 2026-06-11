ALTER TABLE member
    ADD COLUMN referral_code VARCHAR(6) UNIQUE;

CREATE TABLE member_referral_code_usage
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    inviter_id    BIGINT      NOT NULL,
    invitee_id    BIGINT      NOT NULL,
    referral_code VARCHAR(10) NOT NULL,
    created_at    DATETIME    NOT NULL DEFAULT NOW()
);
