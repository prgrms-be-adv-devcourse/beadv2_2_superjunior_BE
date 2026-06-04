-- 스키마 생성
CREATE SCHEMA IF NOT EXISTS product_schema;

-- Product 테이블
CREATE TABLE IF NOT EXISTS product_schema.product (
    product_id UUID PRIMARY KEY,
    seller_id UUID NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    price BIGINT NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    category VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- GroupPurchase 테이블
CREATE TABLE IF NOT EXISTS product_schema.group_purchase (
    group_purchase_id UUID PRIMARY KEY,
    version BIGINT DEFAULT 0,
    min_quantity INT NOT NULL,
    max_quantity INT,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    discounted_price BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    start_date TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date TIMESTAMP WITH TIME ZONE NOT NULL,
    seller_id UUID NOT NULL,
    product_id UUID NOT NULL,
    current_quantity INT NOT NULL DEFAULT 0,
    returned_at TIMESTAMP WITH TIME ZONE,
    succeeded_at TIMESTAMP WITH TIME ZONE,
    settled_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE SCHEMA IF NOT EXISTS settlement_schema;

CREATE TABLE IF NOT EXISTS settlement_schema.order_settlement
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
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    settled_at              TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_order_settlement PRIMARY KEY (order_settlement_id)
);

CREATE TABLE IF NOT EXISTS settlement_schema.seller_balance
(
    balance_id         UUID                     NOT NULL,
    member_id          UUID                     NOT NULL,
    settlement_balance BIGINT                   NOT NULL,
    created_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_seller_balance PRIMARY KEY (balance_id),
    CONSTRAINT uc_seller_balance_member UNIQUE (member_id)
);

CREATE TABLE IF NOT EXISTS settlement_schema.seller_balance_history
(
    history_id          UUID                     NOT NULL,
    member_id           UUID                     NOT NULL,
    seller_payout_id    UUID,
    order_settlement_id UUID,
    amount              BIGINT                   NOT NULL,
    status              VARCHAR(10)              NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_seller_balance_history PRIMARY KEY (history_id),
    CONSTRAINT uc_seller_balance_history_order_settlement UNIQUE (order_settlement_id)
);

CREATE TABLE IF NOT EXISTS settlement_schema.seller_payout
(
    seller_payout_id UUID                     NOT NULL,
    seller_id        UUID                     NOT NULL,
    period_start     TIMESTAMP WITH TIME ZONE NOT NULL,
    period_end       TIMESTAMP WITH TIME ZONE NOT NULL,
    total_amount     BIGINT                   NOT NULL,
    status           VARCHAR(20)              NOT NULL,
    paid_at          TIMESTAMP WITH TIME ZONE,
    account_number   VARCHAR(20),
    bank_code        VARCHAR(20),
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_seller_payout PRIMARY KEY (seller_payout_id)
);

CREATE TABLE IF NOT EXISTS settlement_schema.seller_payout_failure
(
    failure_id       UUID                     NOT NULL,
    seller_payout_id UUID                     NOT NULL,
    seller_id        UUID                     NOT NULL,
    period_start     TIMESTAMP WITH TIME ZONE NOT NULL,
    period_end       TIMESTAMP WITH TIME ZONE NOT NULL,
    failure_reason   TEXT,
    retry_count      INT                      NOT NULL DEFAULT 0,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_seller_payout_failure PRIMARY KEY (failure_id)
);

CREATE SCHEMA IF NOT EXISTS member_schema;

CREATE TABLE IF NOT EXISTS member_schema.seller
(
    seller_id                    UUID        NOT NULL,
    account_number               VARCHAR(20) NOT NULL,
    bank_code                    VARCHAR(20) NOT NULL,
    account_holder               VARCHAR(50) NOT NULL,
    business_registration_number VARCHAR(15) NOT NULL,
    created_at                   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                   TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_seller PRIMARY KEY (seller_id)
);

-- 인덱스
CREATE INDEX IF NOT EXISTS idx_group_purchase_status ON product_schema.group_purchase(status);
CREATE INDEX IF NOT EXISTS idx_group_purchase_start_date ON product_schema.group_purchase(start_date);
CREATE INDEX IF NOT EXISTS idx_group_purchase_end_date ON product_schema.group_purchase(end_date);
