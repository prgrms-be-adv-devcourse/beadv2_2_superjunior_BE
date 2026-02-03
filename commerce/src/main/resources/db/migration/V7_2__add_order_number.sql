ALTER TABLE order_schema."order"
    ADD order_number VARCHAR(255);

ALTER TABLE order_schema."order"
    ALTER COLUMN order_number SET NOT NULL;

ALTER TABLE order_schema."order"
    ADD CONSTRAINT uc_order_order_number UNIQUE (order_number);