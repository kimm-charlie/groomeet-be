ALTER TABLE `service_question_option`
    RENAME TO `service_answer_option`;

ALTER TABLE `request_answer`
    RENAME TO `service_member_answer`;

ALTER TABLE `service_member_answer`
    RENAME COLUMN `service_question_option_id` TO `service_answer_option_id`;