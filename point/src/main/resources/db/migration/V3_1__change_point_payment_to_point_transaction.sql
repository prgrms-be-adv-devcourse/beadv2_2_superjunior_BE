CREATE TABLE payment_schema.point_transaction
(
    id              UUID                                      NOT NULL,
    member_id       UUID                                      NOT NULL,
    status          VARCHAR(20)                               NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() NOT NULL,
    idempotency_key UUID                                      NOT NULL,
    order_id        UUID,
    cancel_reason   VARCHAR(255),
    paid_amount     BIGINT                                    NOT NULL,
    bonus_amount    BIGINT                                    NOT NULL,
    CONSTRAINT pk_point_transaction PRIMARY KEY (id)
);

ALTER TABLE payment_schema.point_transaction
    ADD CONSTRAINT uc_point_transaction UNIQUE (order_id, status);

ALTER TABLE payment_schema.point_transaction
    ADD CONSTRAINT uc_point_transaction_idempotency_key UNIQUE (idempotency_key);

DROP TABLE payment_schema.point_payment CASCADE;
