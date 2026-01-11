ALTER TABLE payment_schema.payment
    ADD CONSTRAINT uc_payment_payment_key UNIQUE (payment_key);

ALTER TABLE payment_schema.payment
    DROP COLUMN pg_order_id;

ALTER TABLE payment_schema.payment
    ALTER COLUMN payment_key TYPE VARCHAR(255) USING (payment_key::VARCHAR(255));

ALTER TABLE payment_schema.payment
    ALTER COLUMN payment_key SET NOT NULL;
