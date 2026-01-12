-- 스키마 생성
CREATE SCHEMA IF NOT EXISTS product_schema;
CREATE SCHEMA IF NOT EXISTS order_schema;

-- product_schema에 필요한 타입 생성
CREATE TYPE product_schema.product_category AS ENUM (
    'ELECTRONICS', 'FASHION', 'FOOD', 'BEAUTY', 'SPORTS', 'HOME', 'BOOKS', 'OTHER'
);

CREATE TYPE product_schema.group_purchase_status AS ENUM (
    'SCHEDULED', 'OPEN', 'SUCCESS', 'FAILED', 'CANCELLED'
);

-- order_schema에 필요한 타입 생성
CREATE TYPE order_schema.order_status AS ENUM (
    'PENDING', 'PAYMENT_COMPLETED', 'ORDER_FAILED', 'CANCELLED',
    'GROUP_PURCHASE_SUCCESS', 'GROUP_PURCHASE_FAIL', 'REVERSED', 'RETURNED'
);

CREATE TYPE order_schema.payment_method AS ENUM (
   'POINT', 'PG'
);

CREATE TABLE IF NOT EXISTS order_schema.shopping_cart (
    shopping_cart_id UUID PRIMARY KEY,
    member_id UUID NOT NULL,
    group_purchase_id UUID NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ
);

-- product 테이블 (GroupPurchase FK 때문에 필요할 수 있음)
CREATE TABLE IF NOT EXISTS product_schema.product (
    product_id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price BIGINT NOT NULL,
    category product_schema.product_category NOT NULL,
    description TEXT,
    stock INTEGER NOT NULL,
    image_url VARCHAR(500),
    seller_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ
);

-- group_purchase 테이블
CREATE TABLE IF NOT EXISTS product_schema.group_purchase (
    group_purchase_id UUID PRIMARY KEY,
    min_quantity INTEGER NOT NULL,
    max_quantity INTEGER,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    discounted_price BIGINT NOT NULL,
    status product_schema.group_purchase_status NOT NULL,
    start_date TIMESTAMPTZ NOT NULL,
    end_date TIMESTAMPTZ NOT NULL,
    seller_id UUID NOT NULL,
    product_id UUID NOT NULL,
    current_quantity INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    settled_at TIMESTAMPTZ,
    returned_at TIMESTAMPTZ,
    FOREIGN KEY (product_id) REFERENCES product_schema.product(product_id)
    );

-- order 테이블
CREATE TABLE IF NOT EXISTS order_schema."order" (
    order_id UUID PRIMARY KEY,
    member_id UUID NOT NULL,
    seller_id UUID NOT NULL,
    group_purchase_id UUID NOT NULL,
    quantity INTEGER NOT NULL,
    price BIGINT NOT NULL,
    status order_schema.order_status NOT NULL DEFAULT 'PENDING',
    address VARCHAR(255) NOT NULL,
    address_detail VARCHAR(255),
    postal_code VARCHAR(20) NOT NULL,
    receiver_name VARCHAR(100) NOT NULL,
    idempotency_key VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,
    returned_at TIMESTAMPTZ
);
