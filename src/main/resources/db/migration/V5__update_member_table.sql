ALTER TABLE `member`
    MODIFY `is_withdrawal` BOOLEAN NOT NULL DEFAULT false,
    MODIFY `device_type` VARCHAR (10) NOT NULL;