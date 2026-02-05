ALTER TABLE payment_schema.pg_payment
    ADD account_number VARCHAR(255);

ALTER TABLE payment_schema.pg_payment
    ADD approve_number VARCHAR(255);

ALTER TABLE payment_schema.pg_payment
    ADD bank_code VARCHAR(255);

ALTER TABLE payment_schema.pg_payment
    ADD card_number VARCHAR(255);

ALTER TABLE payment_schema.pg_payment
    ADD customer_name VARCHAR(255);

ALTER TABLE payment_schema.pg_payment
    ADD depositor_name VARCHAR(255);

ALTER TABLE payment_schema.pg_payment
    ADD installment_plan_months INTEGER;

ALTER TABLE payment_schema.pg_payment
    ADD is_partial_cancelable BOOLEAN;

ALTER TABLE payment_schema.pg_payment
    ADD issuer_code VARCHAR(255);

ALTER TABLE payment_schema.pg_payment
    ADD phone_number VARCHAR(255);

ALTER TABLE payment_schema.pg_payment
    ADD provider VARCHAR(255);

ALTER TABLE payment_schema.pg_payment
    ADD receipt_url VARCHAR(2048);

ALTER TABLE payment_schema.pg_payment
    ADD refunded_amount BIGINT;

ALTER TABLE payment_schema.pg_payment
    ADD transaction_key VARCHAR(255);

ALTER TABLE payment_schema.pg_payment
    ADD webhook_secret VARCHAR(255);

ALTER TABLE payment_schema.pg_payment
    ALTER COLUMN card_number SET NOT NULL;

ALTER TABLE payment_schema.pg_payment
    ALTER COLUMN installment_plan_months SET NOT NULL;

ALTER TABLE payment_schema.pg_payment
    ALTER COLUMN is_partial_cancelable SET NOT NULL;

ALTER TABLE payment_schema.pg_payment
    ALTER COLUMN refunded_amount SET NOT NULL;

ALTER TABLE payment_schema.pg_payment_cancel
    ADD remaining_amount BIGINT;

ALTER TABLE payment_schema.pg_payment_cancel
    ADD transaction_key VARCHAR(255);

ALTER TABLE payment_schema.pg_payment_cancel
    ALTER COLUMN remaining_amount SET NOT NULL;

ALTER TABLE payment_schema.pg_payment
    ALTER COLUMN transaction_key SET NOT NULL;

ALTER TABLE payment_schema.pg_payment_cancel
    ALTER COLUMN transaction_key SET NOT NULL;

ALTER TABLE payment_schema.pg_payment
    DROP COLUMN fail_message;

ALTER TABLE payment_schema.pg_payment
    DROP COLUMN refund_message;
