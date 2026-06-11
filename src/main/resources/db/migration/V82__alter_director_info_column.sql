ALTER TABLE director_info
RENAME COLUMN is_service_detail_exist TO is_profile_detail_exist;

ALTER TABLE director_info
RENAME COLUMN is_account_verified TO is_first_cash_charged;