ALTER TABLE settlement_schema.seller_balance_history
    ADD CONSTRAINT uc_seller_balance_history_order_settlement UNIQUE (order_settlement_id);
