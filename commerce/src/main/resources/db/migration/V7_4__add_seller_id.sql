ALTER TABLE order_schema.canceled_order
    ADD seller_id UUID;

ALTER TABLE order_schema.canceled_order
    ALTER COLUMN seller_id SET NOT NULL;
