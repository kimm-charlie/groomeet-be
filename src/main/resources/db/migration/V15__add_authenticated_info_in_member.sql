ALTER TABLE `member`
    ADD COLUMN `is_authenticated` BOOLEAN NOT NULL DEFAULT 0,
ADD COLUMN `name` VARCHAR(20),
ADD COLUMN `phone_number` VARCHAR(20),
ADD COLUMN `birth` DATE,
ADD COLUMN `gender` VARCHAR(10),
ADD COLUMN `authenticated_at` DATETIME,
ADD COLUMN `authentication_ci` VARCHAR(500),
ADD COLUMN `authentication_di` VARCHAR(500),
ADD COLUMN `nation` VARCHAR(10);