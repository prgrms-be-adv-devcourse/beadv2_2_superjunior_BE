CREATE TABLE payment_schema.bonus_deduction
(
    id               UUID                        NOT NULL,
    bonus_earning_id UUID                        NOT NULL,
    transaction_id   UUID                        NOT NULL,
    amount           BIGINT                      NOT NULL,
    deducted_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_bonus_deduction PRIMARY KEY (id)
);

CREATE INDEX idx_bonus_deduction_earning ON payment_schema.bonus_deduction (bonus_earning_id);

CREATE INDEX idx_bonus_deduction_transaction ON payment_schema.bonus_deduction (transaction_id);

ALTER TABLE payment_schema.bonus_earning
    DROP COLUMN expired_at;

ALTER TABLE payment_schema.bonus_earning
    DROP COLUMN remaining_amount;

ALTER TABLE payment_schema.bonus_earning
    DROP COLUMN used_at;

ALTER TABLE payment_schema.pg_payment
    ALTER COLUMN installment_plan_months DROP NOT NULL;

ALTER TABLE payment_schema.pg_payment
    ALTER COLUMN transaction_key DROP NOT NULL;
