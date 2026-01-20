ALTER TABLE payment_schema.pg_payment_cancel
    DROP COLUMN remaining_amount;

ALTER TABLE payment_schema.pg_payment
    DROP COLUMN webhook_secret;

ALTER TABLE payment_schema.pg_payment
    ALTER COLUMN refunded_amount DROP NOT NULL;
