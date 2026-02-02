ALTER TABLE payment_schema.pg_payment
    ADD group_purchase_name VARCHAR(255);

ALTER TABLE payment_schema.point_transaction
    ADD group_purchase_name VARCHAR(255);
