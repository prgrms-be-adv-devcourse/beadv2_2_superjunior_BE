create schema point_schema;

create table point_schema.payment_point
(
    payment_point_id uuid                                   not null
        constraint payment_point_pk
            primary key,
    member_id        uuid                                   not null,
    pg_order_id      uuid                                   not null
        constraint payment_point_pk_2
            unique,
    payment_method   varchar(30),
    payment_key      varchar(50),
    amount           integer                                not null,
    status           varchar(20)                            not null
        constraint status_check
            check ((status)::text = ANY
                   (ARRAY [('REQUESTED'::character varying)::text, ('COMPLETED'::character varying)::text, ('REFUNDED'::character varying)::text, ('FAILED'::character varying)::text])),
    fail_message     text,
    created_at       timestamp with time zone default now() not null,
    requested_at     timestamp with time zone,
    approved_at      timestamp with time zone,
    updated_at       timestamp with time zone
);

comment on table point_schema.payment_point is '결제';

comment on column point_schema.payment_point.payment_point_id is '결제 ID';

comment on column point_schema.payment_point.member_id is '멤버 ID';

comment on column point_schema.payment_point.pg_order_id is 'PG사 주문 ID';

comment on column point_schema.payment_point.payment_method is '결제 방식';

comment on column point_schema.payment_point.payment_key is 'PG사 결제 키';

comment on column point_schema.payment_point.amount is '금액';

comment on column point_schema.payment_point.status is '상태';

comment on column point_schema.payment_point.fail_message is '실패 메시지';

comment on column point_schema.payment_point.created_at is '생성 시간';

comment on column point_schema.payment_point.requested_at is '결제 요청 시간';

comment on column point_schema.payment_point.approved_at is '결제 승인 시간';

comment on column point_schema.payment_point.updated_at is '수정 시간';

alter table point_schema.payment_point
    owner to postgres;

create table point_schema.payment_point_failure
(
    failure_id       uuid                                   not null
        constraint payment_point_failure_pk
            primary key,
    payment_point_id uuid                                   not null
        constraint payment_point_failure_pk_2
            unique
        constraint payment_point_failure_payment_point_payment_point_id_fk
            references point_schema.payment_point,
    payment_key      varchar(50),
    error_code       varchar(30),
    error_message    text,
    amount           integer,
    raw_payload      text                                   not null,
    created_at       timestamp with time zone default now() not null
);

comment on table point_schema.payment_point_failure is '결제 실패';

comment on column point_schema.payment_point_failure.failure_id is '결제 실패 ID';

comment on column point_schema.payment_point_failure.payment_point_id is '결제 ID';

comment on column point_schema.payment_point_failure.payment_key is 'PG사 결제 키';

comment on column point_schema.payment_point_failure.error_code is '에러 코드';

comment on column point_schema.payment_point_failure.error_message is '에러 메시지';

comment on column point_schema.payment_point_failure.amount is '금액';

comment on column point_schema.payment_point_failure.raw_payload is 'PG사 응답 JSON 데이터';

comment on column point_schema.payment_point_failure.created_at is '생성 시간';

alter table point_schema.payment_point_failure
    owner to postgres;

create table point_schema.member_point
(
    member_id       uuid                                   not null
            constraint member_point_pk
                primary key,
    point_balance           integer
);

comment on table point_schema.member_point is '회원별 보유 포인트';

comment on column point_schema.member_point.member_id is '멤버 ID';

comment on column point_schema.member_point.point_balance is '포인트 잔액';
