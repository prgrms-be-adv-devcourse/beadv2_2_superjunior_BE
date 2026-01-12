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
