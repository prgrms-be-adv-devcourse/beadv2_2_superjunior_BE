CREATE TABLE settlement_schema.order_settlement
(
    order_settlement_id UUID                        NOT NULL,
    order_id            UUID                        NOT NULL,
    seller_id           UUID                        NOT NULL,
    group_purchase_id   UUID                        NOT NULL,
    total_amount        BIGINT                      NOT NULL,
    order_status        VARCHAR(255)                NOT NULL,
    settlement_id       UUID,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    settled_id          TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_order_settlement PRIMARY KEY (order_settlement_id)
);
