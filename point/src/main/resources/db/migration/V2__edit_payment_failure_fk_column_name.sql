ALTER TABLE payment_failure
    DROP CONSTRAINT fk_payment_failure_on_payment_point;

ALTER TABLE payment_failure
    ADD payment_id UUID;

ALTER TABLE payment_failure
    ADD CONSTRAINT uc_payment_failure_payment UNIQUE (payment_id);

ALTER TABLE payment_failure
    ADD CONSTRAINT FK_PAYMENT_FAILURE_ON_PAYMENT FOREIGN KEY (payment_id) REFERENCES payment (id);

ALTER TABLE payment_failure
    DROP COLUMN payment_point_id;
