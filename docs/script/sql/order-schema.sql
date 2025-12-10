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
    group_purchase_id     uuid                                                     not null,
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

comment on column order_schema."order".group_purchase_id is '공동 구매 ID';

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

create table order_schema.settlement
(
    settlement_id     uuid                                   not null
        constraint settlement_pk
            primary key,
    seller_id         uuid                                   not null,
    period_start      timestamp with time zone               not null,
    period_end        timestamp with time zone               not null,
    total_amount      bigint                                 not null,
    service_fee       numeric(12, 2)                         not null,
    settlement_amount numeric(12, 2)                         not null,
    status            varchar(20)                            not null,
    settled_at        timestamp with time zone default now() not null,
    created_at        timestamp with time zone default now() not null,
    updated_at        timestamp with time zone
);

comment on table order_schema.settlement is '정산';

comment on column order_schema.settlement.settlement_id is '정산 ID';

comment on column order_schema.settlement.seller_id is '판매자 ID';

comment on column order_schema.settlement.period_start is '정산 시작일';

comment on column order_schema.settlement.period_end is '정산 종료일';

comment on column order_schema.settlement.total_amount is '판매 총액';

comment on column order_schema.settlement.service_fee is '수수료 합계';

comment on column order_schema.settlement.settlement_amount is '실제 정산 금액';

comment on column order_schema.settlement.status is '정산 상태';

comment on column order_schema.settlement.settled_at is '정산 처리 시각';

comment on column order_schema.settlement.created_at is '생성일';

comment on column order_schema.settlement.updated_at is '수정일';

alter table order_schema.settlement
    owner to postgres;

create table order_schema.seller_balance
(
    balance_id         uuid                                       not null
        constraint seller_balance_pk primary key,
    member_id          uuid                                       not null
        constraint seller_balance_member_unique unique,
    settlement_balance bigint                      default 0      not null,
    created_at         timestamp with time zone    default now()  not null,
    updated_at         timestamp with time zone
);

comment on table order_schema.seller_balance is '판매자 정산 잔액';

comment on column order_schema.seller_balance.balance_id is 'balance id';

comment on column order_schema.seller_balance.member_id is '멤버 id';

comment on column order_schema.seller_balance.settlement_balance is '정산 잔금';

comment on column order_schema.seller_balance.created_at is '생성일';

comment on column order_schema.seller_balance.updated_at is '수정일';

alter table order_schema.seller_balance
    owner to postgres;

create table order_schema.seller_balance_history
(
    history_id         uuid                                       not null
        constraint seller_balance_history_pk primary key,
    member_id          uuid                                       not null,
    settlement_id      uuid                                       not null,
    amount             bigint                                     not null,
    created_at         timestamp with time zone    default now()  not null,
    status             varchar(10)                                not null
        constraint seller_balance_history_status_check
            check ((status)::text = ANY
        (ARRAY[
        ('credit'::character varying)::text,
        ('debit'::character varying)::text
        ]))
);

comment on table order_schema.seller_balance_history is '판매자 정산 잔액 변경 내역';

comment on column order_schema.seller_balance_history.history_id is 'history id';

comment on column order_schema.seller_balance_history.member_id is '멤버 id';

comment on column order_schema.seller_balance_history.amount is '증감된 금액';

comment on column order_schema.seller_balance_history.status is '상태 (credit: 입금/증가, debit: 출금/감소)';

comment on column order_schema.seller_balance_history.created_at is '생성일';

alter table order_schema.seller_balance_history
    owner to postgres;
