ALTER TABLE payment_request
    ADD COLUMN is_additional BOOLEAN DEFAULT FALSE;