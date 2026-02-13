CREATE TABLE settlement_schema.order_settlement
(
    order_settlement_id     UUID                        NOT NULL,
    seller_id               UUID                        NOT NULL,
    group_purchase_id       UUID                        NOT NULL,
    order_id                UUID                        NOT NULL,
    order_settlement_status VARCHAR(255)                NOT NULL,
    order_amount            BIGINT                      NOT NULL,
    platform_fee_rate       DOUBLE PRECISION            NOT NULL,
    platform_fee            BIGINT                      NOT NULL,
    settlement_amount       BIGINT                      NOT NULL,
    created_at              TIMESTAMP WITH    TIME ZONE NOT NULL,
    settled_at              TIMESTAMP WITH    TIME ZONE,
    CONSTRAINT pk_order_settlement PRIMARY KEY (order_settlement_id)
);

CREATE TABLE settlement_schema.seller_balance
(
    balance_id         UUID                        NOT NULL,
    member_id          UUID                        NOT NULL,
    settlement_balance BIGINT                      NOT NULL,
    created_at         TIMESTAMP WITH    TIME ZONE NOT NULL,
    updated_at         TIMESTAMP WITH    TIME ZONE,
    CONSTRAINT pk_seller_balance PRIMARY KEY (balance_id)
);

CREATE TABLE settlement_schema.seller_balance_history
(
    history_id          UUID                        NOT NULL,
    member_id           UUID                        NOT NULL,
    seller_payout_id    UUID,
    order_settlement_id UUID,
    amount              BIGINT                      NOT NULL,
    status              VARCHAR(10)                 NOT NULL,
    created_at          TIMESTAMP WITH    TIME ZONE NOT NULL,
    CONSTRAINT pk_seller_balance_history PRIMARY KEY (history_id)
);

CREATE TABLE settlement_schema.seller_payout
(
    seller_payout_id UUID                        NOT NULL,
    seller_id        UUID                        NOT NULL,
    period_start     TIMESTAMP WITH    TIME ZONE NOT NULL,
    period_end       TIMESTAMP WITH    TIME ZONE NOT NULL,
    total_amount     BIGINT                      NOT NULL,
    status           VARCHAR(255)                NOT NULL,
    paid_at          TIMESTAMP WITH    TIME ZONE,
    created_at       TIMESTAMP WITH    TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITH    TIME ZONE,
    account_number   VARCHAR(255),
    bank_code        VARCHAR(255),
    CONSTRAINT pk_seller_payout PRIMARY KEY (seller_payout_id)
);

CREATE TABLE settlement_schema.seller_payout_failure
(
    failure_id       UUID                        NOT NULL,
    seller_payout_id UUID                        NOT NULL,
    seller_id        UUID                        NOT NULL,
    period_start     TIMESTAMP WITH    TIME ZONE NOT NULL,
    period_end       TIMESTAMP WITH    TIME ZONE NOT NULL,
    failure_reason   VARCHAR(500)                NOT NULL,
    retry_count      INTEGER                     NOT NULL,
    created_at       TIMESTAMP WITH    TIME ZONE NOT NULL,
    CONSTRAINT pk_seller_payout_failure PRIMARY KEY (failure_id)
);

ALTER TABLE settlement_schema.seller_balance_history
    ADD CONSTRAINT uc_seller_balance_history_order_settlement UNIQUE (order_settlement_id);

ALTER TABLE settlement_schema.seller_balance
    ADD CONSTRAINT uc_seller_balance_member UNIQUE (member_id);
