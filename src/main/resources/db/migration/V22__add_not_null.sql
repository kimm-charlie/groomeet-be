ALTER TABLE `service_question`
    MODIFY COLUMN `question_order` INTEGER NOT NULL DEFAULT 0;

ALTER TABLE `service_answer_option`
    MODIFY COLUMN `content` VARCHAR (100) NOT NULL,
    MODIFY COLUMN `option_order` INTEGER NOT NULL DEFAULT 0;
