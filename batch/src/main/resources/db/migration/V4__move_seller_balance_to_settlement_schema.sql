DROP TABLE order_schema.seller_balance CASCADE;

DROP TABLE order_schema.seller_balance_history CASCADE;

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
    settlement_id       UUID,
    order_settlement_id UUID,
    amount              BIGINT                      NOT NULL,
    status              VARCHAR(10)                 NOT NULL,
    created_at          TIMESTAMP WITH    TIME ZONE NOT NULL,
    CONSTRAINT pk_seller_balance_history PRIMARY KEY (history_id)
);

ALTER TABLE settlement_schema.seller_balance
    ADD CONSTRAINT uc_seller_balance_member UNIQUE (member_id);
