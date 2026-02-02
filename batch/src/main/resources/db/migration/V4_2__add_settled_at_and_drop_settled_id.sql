ALTER TABLE settlement_schema.order_settlement
    ADD settled_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE settlement_schema.order_settlement
    DROP COLUMN settled_id;
