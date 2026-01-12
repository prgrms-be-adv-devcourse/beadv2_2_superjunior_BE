CREATE TABLE payment_schema.payment
(
    id             UUID                        NOT NULL,
    member_id      UUID                        NOT NULL,
    pg_order_id    UUID                        NOT NULL,
    order_id       UUID                        NOT NULL,
    payment_method VARCHAR(30),
    payment_key    VARCHAR(50),
    amount         BIGINT                      NOT NULL,
    status         VARCHAR(20)                 NOT NULL,
    fail_message   VARCHAR(255),
    refund_message VARCHAR(255),
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    requested_at   TIMESTAMP WITHOUT TIME ZONE,
    approved_at    TIMESTAMP WITHOUT TIME ZONE,
    refunded_at    TIMESTAMP WITHOUT TIME ZONE,
    updated_at     TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_payment PRIMARY KEY (id)
);

CREATE TABLE payment_schema.payment_cancel
(
    id            UUID                        NOT NULL,
    payment_id    UUID                        NOT NULL,
    cancel_amount BIGINT                      NOT NULL,
    cancel_reason VARCHAR(255),
    pg_cancel_key VARCHAR(255),
    canceled_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_payment_cancel PRIMARY KEY (id)
);

CREATE TABLE payment_schema.payment_failure
(
    id               UUID                        NOT NULL,
    payment_point_id UUID,
    payment_key      VARCHAR(50)                 NOT NULL,
    error_code       VARCHAR(30),
    error_message    VARCHAR(255),
    amount           BIGINT,
    raw_payload      TEXT                        NOT NULL,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_payment_failure PRIMARY KEY (id)
);

CREATE TABLE payment_schema.point
(
    member_id    UUID   NOT NULL,
    paid_point   BIGINT NOT NULL,
    bonus_point  BIGINT NOT NULL,
    last_used_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_point PRIMARY KEY (member_id)
);

CREATE TABLE payment_schema.point_history
(
    id              UUID                                      NOT NULL,
    member_id       UUID                                      NOT NULL,
    status          VARCHAR(20)                               NOT NULL,
    amount          BIGINT                                    NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() NOT NULL,
    idempotency_key UUID                                      NOT NULL,
    order_id        UUID                                      NOT NULL,
    CONSTRAINT pk_point_history PRIMARY KEY (id)
);

ALTER TABLE payment_schema.point_history
    ADD CONSTRAINT point_history_pk_3 UNIQUE (order_id, status);

ALTER TABLE payment_schema.payment_failure
    ADD CONSTRAINT uc_payment_failure_payment_point UNIQUE (payment_point_id);

ALTER TABLE payment_schema.payment
    ADD CONSTRAINT uc_payment_order UNIQUE (order_id);

ALTER TABLE payment_schema.payment
    ADD CONSTRAINT uc_payment_pg_order UNIQUE (pg_order_id);

ALTER TABLE payment_schema.point_history
    ADD CONSTRAINT uc_point_history_idempotency_key UNIQUE (idempotency_key);

ALTER TABLE payment_schema.payment_cancel
    ADD CONSTRAINT FK_PAYMENT_CANCEL_ON_PAYMENT FOREIGN KEY (payment_id) REFERENCES payment_schema.payment (id);

ALTER TABLE payment_schema.payment_failure
    ADD CONSTRAINT FK_PAYMENT_FAILURE_ON_PAYMENT_POINT FOREIGN KEY (payment_point_id) REFERENCES payment_schema.payment (id);
