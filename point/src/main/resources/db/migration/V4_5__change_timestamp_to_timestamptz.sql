-- pg_payment 테이블
ALTER TABLE payment_schema.pg_payment
    ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE;

ALTER TABLE payment_schema.pg_payment
    ALTER COLUMN requested_at TYPE TIMESTAMP WITH TIME ZONE;

ALTER TABLE payment_schema.pg_payment
    ALTER COLUMN approved_at TYPE TIMESTAMP WITH TIME ZONE;

ALTER TABLE payment_schema.pg_payment
    ALTER COLUMN refunded_at TYPE TIMESTAMP WITH TIME ZONE;

ALTER TABLE payment_schema.pg_payment
    ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE;

-- pg_payment_cancel 테이블
ALTER TABLE payment_schema.pg_payment_cancel
    ALTER COLUMN canceled_at TYPE TIMESTAMP WITH TIME ZONE;

-- pg_payment_failure 테이블
ALTER TABLE payment_schema.pg_payment_failure
    ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE;

-- point_balance 테이블
ALTER TABLE payment_schema.point_balance
    ALTER COLUMN last_used_at TYPE TIMESTAMP WITH TIME ZONE;

-- point_transaction 테이블
ALTER TABLE payment_schema.point_transaction
    ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE;

-- bonus_earning 테이블
ALTER TABLE payment_schema.bonus_earning
    ALTER COLUMN expires_at TYPE TIMESTAMP WITH TIME ZONE;

ALTER TABLE payment_schema.bonus_earning
    ALTER COLUMN earned_at TYPE TIMESTAMP WITH TIME ZONE;

-- bonus_deduction 테이블
ALTER TABLE payment_schema.bonus_deduction
    ALTER COLUMN deducted_at TYPE TIMESTAMP WITH TIME ZONE;

-- bonus_policy 테이블
ALTER TABLE payment_schema.bonus_policy
    ALTER COLUMN valid_from TYPE TIMESTAMP WITH TIME ZONE;

ALTER TABLE payment_schema.bonus_policy
    ALTER COLUMN valid_until TYPE TIMESTAMP WITH TIME ZONE;
