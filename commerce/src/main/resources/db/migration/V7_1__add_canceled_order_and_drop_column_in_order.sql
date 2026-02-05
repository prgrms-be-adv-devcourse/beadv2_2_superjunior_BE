CREATE TABLE order_schema.canceled_order
(
    cancel_order_id UUID         NOT NULL,
    order_id        UUID         NOT NULL,
    member_id       UUID         NOT NULL,
    returned_amount BIGINT       NOT NULL,
    fee             BIGINT       NOT NULL,
    status          VARCHAR(255) NOT NULL,
    reason          VARCHAR(255) NOT NULL,
    detail_reason   VARCHAR(255),
    idempotency_key VARCHAR(255) NOT NULL,
    payment_method  VARCHAR(255),
    canceled_at     TIMESTAMP WITH TIME ZONE,
    returned_at     TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_canceled_order PRIMARY KEY (cancel_order_id)
);

ALTER TABLE order_schema."order"
    ADD canceled_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE order_schema.canceled_order
    ADD CONSTRAINT uc_canceled_order_idempotency_key UNIQUE (idempotency_key);

ALTER TABLE order_schema.canceled_order
    ADD CONSTRAINT uc_canceled_order_order UNIQUE (order_id);

ALTER TABLE order_schema."order"
DROP
COLUMN cancelled_at;