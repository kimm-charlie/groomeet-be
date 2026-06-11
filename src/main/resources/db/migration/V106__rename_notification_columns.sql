ALTER TABLE member
    CHANGE is_chat_notification_agreed is_activity_push_agreed TINYINT(1) NOT NULL DEFAULT 0;

ALTER TABLE member
    CHANGE is_marketing_notification_agreed is_marketing_push_agreed TINYINT(1) NOT NULL DEFAULT 0;
