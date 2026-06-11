UPDATE member
SET nickname = CONCAT('GROO_', SUBSTRING(nickname, 6))
WHERE nickname LIKE 'MOTD_%';
