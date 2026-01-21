ALTER TABLE order_schema."order"
    ADD cancel_requested_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE order_schema."order"
    ADD cancelled_at TIMESTAMP WITH TIME ZONE;
