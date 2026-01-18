CREATE TABLE settlement_schema.settlement
(
    settlement_id     UUID                        NOT NULL,
    seller_id         UUID                        NOT NULL,
    period_start      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    period_end        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    total_amount      BIGINT                      NOT NULL,
    status            VARCHAR(255)                NOT NULL,
    service_fee       DECIMAL(12, 2)              NOT NULL,
    settlement_amount DECIMAL(12, 2)              NOT NULL,
    settled_at        TIMESTAMP WITHOUT TIME ZONE,
    created_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at        TIMESTAMP WITHOUT TIME ZONE,
    account_number    VARCHAR(255),
    bank_code         VARCHAR(255),
    CONSTRAINT pk_settlement PRIMARY KEY (settlement_id)
);

CREATE TABLE settlement_schema.settlement_failure
(
    failure_id     UUID                        NOT NULL,
    seller_id      UUID                        NOT NULL,
    period_start   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    period_end     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    failure_reason VARCHAR(500)                NOT NULL,
    retry_count    INTEGER                     NOT NULL,
    settlement_id  UUID                        NOT NULL,
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_settlement_failure PRIMARY KEY (failure_id)
);
