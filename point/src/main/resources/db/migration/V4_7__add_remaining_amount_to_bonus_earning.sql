ALTER TABLE payment_schema.bonus_earning
    ADD remaining_amount BIGINT;

UPDATE payment_schema.bonus_earning
SET remaining_amount = amount
WHERE remaining_amount IS NULL;

ALTER TABLE payment_schema.bonus_earning
    ALTER COLUMN remaining_amount SET NOT NULL;
