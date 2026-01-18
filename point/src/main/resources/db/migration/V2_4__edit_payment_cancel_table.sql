ALTER TABLE payment_schema.payment_cancel
    ADD payment_key VARCHAR(255);

ALTER TABLE payment_schema.payment_cancel
    ALTER COLUMN payment_key SET NOT NULL;

ALTER TABLE payment_schema.payment_cancel
    ADD CONSTRAINT uc_payment_cancel_payment_key UNIQUE (payment_key);

ALTER TABLE payment_schema.payment_cancel
    DROP COLUMN pg_cancel_key;

ALTER TABLE payment_schema.point_history
    ALTER COLUMN order_id DROP NOT NULL;
