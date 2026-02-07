ALTER TABLE settlement_schema.order_settlement
    ADD order_settlement_status VARCHAR(255);

ALTER TABLE settlement_schema.order_settlement
    ALTER COLUMN order_settlement_status SET NOT NULL;

ALTER TABLE settlement_schema.order_settlement
    DROP COLUMN order_status;

ALTER TABLE settlement_schema.order_settlement
    DROP COLUMN settlement_id;
