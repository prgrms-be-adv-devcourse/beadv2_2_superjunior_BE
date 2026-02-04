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

-- 인덱스
CREATE INDEX IF NOT EXISTS idx_group_purchase_status ON product_schema.group_purchase(status);
CREATE INDEX IF NOT EXISTS idx_group_purchase_start_date ON product_schema.group_purchase(start_date);
CREATE INDEX IF NOT EXISTS idx_group_purchase_end_date ON product_schema.group_purchase(end_date);
