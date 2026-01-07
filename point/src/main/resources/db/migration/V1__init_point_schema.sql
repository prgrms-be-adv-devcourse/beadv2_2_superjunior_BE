CREATE TABLE point_schema.member_point
(
    member_id     UUID   NOT NULL,
    point_balance BIGINT NOT NULL,
    last_used_at  TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_member_point PRIMARY KEY (member_id)
);

CREATE TABLE point_schema.member_point_history
(
    id              UUID        NOT NULL,
    member_id       UUID        NOT NULL,
    status          VARCHAR(20) NOT NULL,
    amount          BIGINT      NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() NOT NULL,
    idempotency_key UUID        NOT NULL,
    order_id        UUID        NOT NULL,
    CONSTRAINT pk_member_point_history PRIMARY KEY (id)
);

CREATE TABLE point_schema.payment_point
(
    payment_point_id UUID        NOT NULL,
    member_id        UUID        NOT NULL,
    pg_order_id      UUID        NOT NULL,
    payment_method   VARCHAR(30),
    payment_key      VARCHAR(50),
    amount           BIGINT      NOT NULL,
    status           VARCHAR(20) NOT NULL,
    fail_message     VARCHAR(255),
    refund_message   VARCHAR(255),
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    requested_at     TIMESTAMP WITHOUT TIME ZONE,
    approved_at      TIMESTAMP WITHOUT TIME ZONE,
    refunded_at      TIMESTAMP WITHOUT TIME ZONE,
    updated_at       TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_payment_point PRIMARY KEY (payment_point_id)
);

CREATE TABLE point_schema.payment_point_failure
(
    failure_id       UUID        NOT NULL,
    payment_point_id UUID,
    payment_key      VARCHAR(50) NOT NULL,
    error_code       VARCHAR(30),
    error_message    VARCHAR(255),
    amount           BIGINT,
    raw_payload      TEXT        NOT NULL,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_payment_point_failure PRIMARY KEY (failure_id)
);

ALTER TABLE point_schema.member_point_history
    ADD CONSTRAINT member_point_history_pk_3 UNIQUE (order_id, status);

ALTER TABLE point_schema.member_point_history
    ADD CONSTRAINT uc_member_point_history_idempotency_key UNIQUE (idempotency_key);

ALTER TABLE point_schema.payment_point_failure
    ADD CONSTRAINT uc_payment_point_failure_payment_point UNIQUE (payment_point_id);

ALTER TABLE point_schema.payment_point
    ADD CONSTRAINT uc_payment_point_pg_order UNIQUE (pg_order_id);

ALTER TABLE point_schema.payment_point_failure
    ADD CONSTRAINT FK_PAYMENT_POINT_FAILURE_ON_PAYMENT_POINT FOREIGN KEY (payment_point_id) REFERENCES point_schema.payment_point (payment_point_id);
