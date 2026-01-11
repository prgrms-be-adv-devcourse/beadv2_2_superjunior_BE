ALTER TABLE payment_schema.payment_failure
    ADD CONSTRAINT uc_payment_failure_payment_key UNIQUE (payment_key);

ALTER TABLE payment_schema.payment_failure
    ALTER COLUMN payment_key TYPE VARCHAR(255) USING (payment_key::VARCHAR(255));
