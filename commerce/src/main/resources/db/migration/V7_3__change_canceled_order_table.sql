ALTER TABLE order_schema.canceled_order
    ADD cancel_fee_amount BIGINT;

ALTER TABLE order_schema.canceled_order
    ADD original_paid_amount BIGINT;

ALTER TABLE order_schema.canceled_order
    ADD policy_id VARCHAR(60);

ALTER TABLE order_schema.canceled_order
    ADD policy_snapshot TEXT;

ALTER TABLE order_schema.canceled_order
    ADD refund_amount BIGINT;

ALTER TABLE order_schema.canceled_order
    ADD shipping_fee_amount BIGINT;

ALTER TABLE order_schema.canceled_order
    ALTER COLUMN cancel_fee_amount SET NOT NULL;

ALTER TABLE order_schema.canceled_order
    ALTER COLUMN original_paid_amount SET NOT NULL;

ALTER TABLE order_schema."order"
    ADD paid_price BIGINT;

ALTER TABLE order_schema."order"
    ALTER COLUMN paid_price SET NOT NULL;

ALTER TABLE order_schema.canceled_order
    ALTER COLUMN policy_id SET NOT NULL;

ALTER TABLE order_schema.canceled_order
    ALTER COLUMN refund_amount SET NOT NULL;

ALTER TABLE order_schema.canceled_order
    ALTER COLUMN shipping_fee_amount SET NOT NULL;

ALTER TABLE order_schema."order"
    DROP COLUMN cancel_requested_at;

ALTER TABLE order_schema."order"
    DROP COLUMN returned_at;

ALTER TABLE order_schema.canceled_order
    DROP COLUMN fee;

ALTER TABLE order_schema.canceled_order
    DROP COLUMN returned_amount;
