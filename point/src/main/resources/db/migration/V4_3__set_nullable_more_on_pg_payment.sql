ALTER TABLE payment_schema.pg_payment
    ALTER COLUMN installment_plan_months DROP NOT NULL;

ALTER TABLE payment_schema.pg_payment
    ALTER COLUMN transaction_key DROP NOT NULL;
