ALTER TABLE payment_schema.bonus_earning
    ADD version BIGINT DEFAULT 0;

ALTER TABLE payment_schema.bonus_earning
    ALTER COLUMN version SET NOT NULL;

ALTER TABLE payment_schema.pg_payment
    ADD version BIGINT DEFAULT 0;

ALTER TABLE payment_schema.pg_payment
    ALTER COLUMN version SET NOT NULL;

ALTER TABLE payment_schema.point_balance
    ADD version BIGINT DEFAULT 0;

ALTER TABLE payment_schema.point_balance
    ALTER COLUMN version SET NOT NULL;

ALTER TABLE payment_schema.pg_payment
    ALTER COLUMN created_at SET DEFAULT NOW();
