CREATE TABLE order_schema."order"
(
    order_id          UUID         NOT NULL,
    quantity          INTEGER      NOT NULL,
    price             BIGINT       NOT NULL,
    status            VARCHAR(255) NOT NULL,
    member_id         UUID         NOT NULL,
    address           VARCHAR(100) NOT NULL,
    address_detail    VARCHAR(100) NOT NULL,
    postal_code       VARCHAR(50)  NOT NULL,
    receiver_name     VARCHAR(100) NOT NULL,
    seller_id         UUID         NOT NULL,
    group_purchase_id UUID         NOT NULL,
    idempotency_key   VARCHAR(255) NOT NULL,
    payment_method    VARCHAR(255),
    expired_at        TIMESTAMP WITHOUT TIME ZONE,
    paid_at           TIMESTAMP WITHOUT TIME ZONE,
    created_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at        TIMESTAMP WITHOUT TIME ZONE,
    deleted_at        TIMESTAMP WITHOUT TIME ZONE,
    returned_at       TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_order PRIMARY KEY (order_id)
);

CREATE TABLE order_schema.seller_balance
(
    balance_id         UUID   NOT NULL,
    member_id          UUID   NOT NULL,
    settlement_balance BIGINT NOT NULL,
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at         TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_seller_balance PRIMARY KEY (balance_id)
);

CREATE TABLE order_schema.seller_balance_history
(
    history_id        UUID        NOT NULL,
    member_id         UUID        NOT NULL,
    settlement_id     UUID,
    group_purchase_id UUID,
    amount            BIGINT      NOT NULL,
    status            VARCHAR(10) NOT NULL,
    created_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_seller_balance_history PRIMARY KEY (history_id)
);

CREATE TABLE order_schema.shopping_cart
(
    shopping_cart_id  UUID    NOT NULL,
    member_id         UUID    NOT NULL,
    group_purchase_id UUID    NOT NULL,
    quantity          INTEGER NOT NULL,
    created_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at        TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_shopping_cart PRIMARY KEY (shopping_cart_id)
);

ALTER TABLE order_schema."order"
    ADD CONSTRAINT uc_order_idempotency_key UNIQUE (idempotency_key);

ALTER TABLE order_schema.seller_balance
    ADD CONSTRAINT uc_seller_balance_member UNIQUE (member_id);

CREATE TABLE product_schema.group_purchase
(
    group_purchase_id UUID         NOT NULL,
    min_quantity      INTEGER      NOT NULL,
    max_quantity      INTEGER,
    title             VARCHAR(100) NOT NULL,
    description       VARCHAR(255),
    discounted_price  BIGINT       NOT NULL,
    status            VARCHAR(255) NOT NULL,
    start_date        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_date          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    seller_id         UUID         NOT NULL,
    product_id        UUID         NOT NULL,
    current_quantity  INTEGER      NOT NULL,
    created_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at        TIMESTAMP WITHOUT TIME ZONE,
    settled_at        TIMESTAMP WITHOUT TIME ZONE,
    returned_at       TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_group_purchase PRIMARY KEY (group_purchase_id)
);

CREATE TABLE product_schema.product
(
    product_id   UUID         NOT NULL,
    name         VARCHAR(100) NOT NULL,
    price        BIGINT       NOT NULL,
    category     VARCHAR(255) NOT NULL,
    description  TEXT         NOT NULL,
    stock        INTEGER      NOT NULL,
    original_url VARCHAR(255),
    seller_id    UUID         NOT NULL,
    created_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITHOUT TIME ZONE,
    deleted_at   TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_product PRIMARY KEY (product_id)
);
