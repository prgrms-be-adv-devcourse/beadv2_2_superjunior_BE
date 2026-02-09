CREATE TABLE settlement_schema.seller_payout
(
    seller_payout_id UUID                        NOT NULL,
    seller_id        UUID                        NOT NULL,
    period_start     TIMESTAMP WITH TIME ZONE    NOT NULL,
    period_end       TIMESTAMP WITH TIME ZONE    NOT NULL,
    total_amount     BIGINT                      NOT NULL,
    status           VARCHAR(255)                NOT NULL,
    paid_at          TIMESTAMP WITH TIME ZONE,
    created_at       TIMESTAMP WITH TIME ZONE    NOT NULL,
    updated_at       TIMESTAMP WITH TIME ZONE,
    account_number   VARCHAR(255),
    bank_code        VARCHAR(255),
    CONSTRAINT pk_seller_payout PRIMARY KEY (seller_payout_id)
);

CREATE TABLE settlement_schema.seller_payout_failure
(
    failure_id       UUID                        NOT NULL,
    seller_payout_id UUID                        NOT NULL,
    seller_id        UUID                        NOT NULL,
    period_start     TIMESTAMP WITH TIME ZONE    NOT NULL,
    period_end       TIMESTAMP WITH TIME ZONE    NOT NULL,
    failure_reason   VARCHAR(500)                NOT NULL,
    retry_count      INTEGER                     NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE    NOT NULL,
    CONSTRAINT pk_seller_payout_failure PRIMARY KEY (failure_id)
);

ALTER TABLE settlement_schema.order_settlement
    ADD order_amount BIGINT;

ALTER TABLE settlement_schema.order_settlement
    ADD platform_fee BIGINT;

ALTER TABLE settlement_schema.order_settlement
    ADD platform_fee_rate DOUBLE PRECISION;

ALTER TABLE settlement_schema.order_settlement
    ADD settlement_amount BIGINT;

ALTER TABLE settlement_schema.order_settlement
    ALTER COLUMN order_amount SET NOT NULL;

ALTER TABLE settlement_schema.order_settlement
    ALTER COLUMN platform_fee SET NOT NULL;

ALTER TABLE settlement_schema.order_settlement
    ALTER COLUMN platform_fee_rate SET NOT NULL;

ALTER TABLE settlement_schema.seller_balance_history
    ADD seller_payout_id UUID;

ALTER TABLE settlement_schema.order_settlement
    ALTER COLUMN settlement_amount SET NOT NULL;

DROP TABLE settlement_schema.settlement CASCADE;

DROP TABLE settlement_schema.settlement_failure CASCADE;

ALTER TABLE settlement_schema.seller_balance_history
    DROP COLUMN settlement_id;

ALTER TABLE settlement_schema.order_settlement
    DROP COLUMN total_amount;
