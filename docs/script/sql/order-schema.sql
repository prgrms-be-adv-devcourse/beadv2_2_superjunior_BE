create schema order_schema;

create table order_schema."order"
(
    order_id       uuid                                                            not null
        constraint order_pk
            primary key,
    quantity       integer                  default 1                              not null,
    price          integer                  default 0                              not null,
    status         varchar(20)              default 'SCHEDULED'::character varying not null
        constraint status_check
            check ((status)::text = ANY
                   (ARRAY [('SCHEDULED'::character varying)::text, ('IN_PROGRESS'::character varying)::text, ('SUCCESS'::character varying)::text, ('FAILED'::character varying)::text])),
    member_id      uuid                                                            not null,
    address        varchar(100)                                                    not null,
    address_detail varchar(100)                                                    not null,
    postal_code    varchar(50)                                                     not null,
    receiver_name  varchar(100),
    seller_id      uuid                                                            not null,
    product_id     uuid                                                            not null,
    created_at     timestamp with time zone default now()                          not null,
    updated_at     timestamp with time zone,
    deleted_at     timestamp with time zone
);

comment on table order_schema."order" is '주문';

comment on column order_schema."order".order_id is '주문 ID';

comment on column order_schema."order".quantity is '수량';

comment on column order_schema."order".price is '결제 포인트';

comment on column order_schema."order".status is '공구 상태';

comment on column order_schema."order".member_id is '구매자 ID';

comment on column order_schema."order".address is '주소';

comment on column order_schema."order".address_detail is '상세 주소';

comment on column order_schema."order".postal_code is '우편 번호';

comment on column order_schema."order".receiver_name is '수신자 이름';

comment on column order_schema."order".seller_id is '판매자 ID';

comment on column order_schema."order".product_id is '상품 ID';

comment on column order_schema."order".created_at is '등록일';

comment on column order_schema."order".updated_at is '수정일';

comment on column order_schema."order".deleted_at is '주문 취소일';

alter table order_schema."order"
    owner to postgres;

create table order_schema.shopping_cart
(
    shopping_cart_id  uuid              not null
        constraint shopping_cart_pk
            primary key,
    member_id         uuid              not null
        constraint shopping_cart_pk_2
            unique,
    group_purchase_id uuid              not null,
    quantity          integer default 1 not null
);

comment on table order_schema.shopping_cart is '장바구니';

comment on column order_schema.shopping_cart.shopping_cart_id is '장바구니 ID';

comment on column order_schema.shopping_cart.member_id is '멤버 ID';

comment on column order_schema.shopping_cart.group_purchase_id is '공동 구매 ID';

comment on column order_schema.shopping_cart.quantity is '수량';

alter table order_schema.shopping_cart
    owner to postgres;
