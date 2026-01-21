ALTER TABLE order_schema."order"
DROP
COLUMN settled_at;

ALTER TABLE product_schema.product
    ADD idempotency_key VARCHAR(255);

ALTER TABLE product_schema.product
    ALTER COLUMN idempotency_key SET NOT NULL;

ALTER TABLE product_schema.group_purchase
    ADD settled_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE product_schema.product
    ADD CONSTRAINT uc_product_idempotency_key UNIQUE (idempotency_key);