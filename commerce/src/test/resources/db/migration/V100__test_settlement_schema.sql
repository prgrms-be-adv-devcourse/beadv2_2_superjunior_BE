-- Commerce 테스트용 settlement_schema 테이블 (batch 서비스 마이그레이션 최종 스키마)

-- 스키마가 없으면 생성
CREATE SCHEMA IF NOT EXISTS settlement_schema;

CREATE TABLE settlement_schema.seller_balance
(
    balance_id         UUID                     NOT NULL,
    member_id          UUID                     NOT NULL,
    settlement_balance BIGINT                   NOT NULL,
    created_at         TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at         TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_seller_balance PRIMARY KEY (balance_id),
    CONSTRAINT uc_seller_balance_member UNIQUE (member_id)
);

CREATE TABLE settlement_schema.seller_balance_history
(
    history_id          UUID                     NOT NULL,
    member_id           UUID                     NOT NULL,
    seller_payout_id    UUID,
    order_settlement_id UUID                     UNIQUE,
    amount              BIGINT                   NOT NULL,
    status              VARCHAR(10)              NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_seller_balance_history PRIMARY KEY (history_id)
);

CREATE TABLE settlement_schema.order_settlement
(
    order_settlement_id     UUID                     NOT NULL,
    seller_id               UUID                     NOT NULL,
    group_purchase_id       UUID                     NOT NULL,
    order_id                UUID                     NOT NULL,
    order_settlement_status VARCHAR(255)             NOT NULL,
    order_amount            BIGINT                   NOT NULL,
    platform_fee_rate       DOUBLE PRECISION         NOT NULL,
    platform_fee            BIGINT                   NOT NULL,
    settlement_amount       BIGINT                   NOT NULL,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL,
    settled_at              TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_order_settlement PRIMARY KEY (order_settlement_id)
);
