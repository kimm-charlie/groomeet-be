ALTER TABLE service_answer_option
    MODIFY content VARCHAR(100) NOT NULL;

ALTER TABLE service_member_answer
    MODIFY content VARCHAR(100) NULL;

ALTER TABLE portfolio
ADD COLUMN work_duration_unit VARCHAR(10) NOT NULL;