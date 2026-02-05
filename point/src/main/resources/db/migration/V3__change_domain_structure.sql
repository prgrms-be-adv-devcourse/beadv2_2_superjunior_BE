ALTER TABLE payment_schema.payment_cancel
    DROP CONSTRAINT fk_payment_cancel_on_payment;

ALTER TABLE payment_schema.payment_failure
    DROP CONSTRAINT fk_payment_failure_on_payment;

CREATE TABLE payment_schema.pg_payment
(
    id             UUID                        NOT NULL,
    member_id      UUID                        NOT NULL,
    order_id       UUID                        NOT NULL,
    payment_method VARCHAR(30),
    payment_key    VARCHAR(255)                NOT NULL,
    amount         BIGINT                      NOT NULL,
    status         VARCHAR(20)                 NOT NULL,
    fail_message   VARCHAR(255),
    refund_message VARCHAR(255),
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    requested_at   TIMESTAMP WITHOUT TIME ZONE,
    approved_at    TIMESTAMP WITHOUT TIME ZONE,
    refunded_at    TIMESTAMP WITHOUT TIME ZONE,
    updated_at     TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_pg_payment PRIMARY KEY (id)
);

CREATE TABLE payment_schema.pg_payment_cancel
(
    id            UUID                        NOT NULL,
    payment_id    UUID                        NOT NULL,
    cancel_amount BIGINT                      NOT NULL,
    cancel_reason VARCHAR(255),
    payment_key   VARCHAR(255)                NOT NULL,
    canceled_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_pg_payment_cancel PRIMARY KEY (id)
);

CREATE TABLE payment_schema.pg_payment_failure
(
    id            UUID                        NOT NULL,
    payment_id    UUID,
    payment_key   VARCHAR(255)                NOT NULL,
    error_code    VARCHAR(30),
    error_message VARCHAR(255),
    amount        BIGINT,
    raw_payload   TEXT                        NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_pg_payment_failure PRIMARY KEY (id)
);

CREATE TABLE payment_schema.point_balance
(
    id           UUID   NOT NULL,
    member_id    UUID   NOT NULL,
    paid_point   BIGINT NOT NULL,
    bonus_point  BIGINT NOT NULL,
    last_used_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_point_balance PRIMARY KEY (id)
);

CREATE TABLE payment_schema.point_payment
(
    id              UUID                                      NOT NULL,
    member_id       UUID                                      NOT NULL,
    status          VARCHAR(20)                               NOT NULL,
    paid_amount     BIGINT                                    NOT NULL,
    bonus_amount    BIGINT                                    NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() NOT NULL,
    idempotency_key UUID                                      NOT NULL,
    order_id        UUID,
    cancel_reason   VARCHAR(255),
    CONSTRAINT pk_point_payment PRIMARY KEY (id)
);

ALTER TABLE payment_schema.point_payment
    ADD CONSTRAINT point_payment_uc UNIQUE (order_id, status);

ALTER TABLE payment_schema.pg_payment_cancel
    ADD CONSTRAINT uc_pg_payment_cancel_payment_key UNIQUE (payment_key);

ALTER TABLE payment_schema.pg_payment_failure
    ADD CONSTRAINT uc_pg_payment_failure_payment UNIQUE (payment_id);

ALTER TABLE payment_schema.pg_payment_failure
    ADD CONSTRAINT uc_pg_payment_failure_payment_key UNIQUE (payment_key);

ALTER TABLE payment_schema.pg_payment
    ADD CONSTRAINT uc_pg_payment_order UNIQUE (order_id);

ALTER TABLE payment_schema.pg_payment
    ADD CONSTRAINT uc_pg_payment_payment_key UNIQUE (payment_key);

ALTER TABLE payment_schema.point_balance
    ADD CONSTRAINT uc_point_balance_member UNIQUE (member_id);

ALTER TABLE payment_schema.point_payment
    ADD CONSTRAINT uc_point_payment_idempotency_key UNIQUE (idempotency_key);

ALTER TABLE payment_schema.pg_payment_cancel
    ADD CONSTRAINT FK_PG_PAYMENT_CANCEL_ON_PAYMENT FOREIGN KEY (payment_id) REFERENCES payment_schema.pg_payment (id);

ALTER TABLE payment_schema.pg_payment_failure
    ADD CONSTRAINT FK_PG_PAYMENT_FAILURE_ON_PAYMENT FOREIGN KEY (payment_id) REFERENCES payment_schema.pg_payment (id);

DROP TABLE payment_schema.payment CASCADE;

DROP TABLE payment_schema.payment_cancel CASCADE;

DROP TABLE payment_schema.payment_failure CASCADE;

DROP TABLE payment_schema.point CASCADE;

DROP TABLE payment_schema.point_history CASCADE;
