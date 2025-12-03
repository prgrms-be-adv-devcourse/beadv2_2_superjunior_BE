create schema settlement_schema;

create table settlement_schema.settlement
(
    settlement_id     uuid                                   not null
        constraint settlement_pk
            primary key,
    seller_id         uuid                                   not null,
    period_start      timestamp with time zone               not null,
    period_end        timestamp with time zone               not null,
    total_amount      integer                                not null,
    service_fee       numeric(12, 2)                         not null,
    settlement_amount numeric(12, 2)                         not null,
    status            varchar(20)                            not null,
    settled_at        timestamp with time zone default now() not null,
    created_at        timestamp with time zone default now() not null,
    updated_at        timestamp with time zone
);

comment on table settlement_schema.settlement is '정산';

comment on column settlement_schema.settlement.settlement_id is '정산 ID';

comment on column settlement_schema.settlement.seller_id is '판매자 ID';

comment on column settlement_schema.settlement.period_start is '정산 시작일';

comment on column settlement_schema.settlement.period_end is '정산 종료일';

comment on column settlement_schema.settlement.total_amount is '판매 총액';

comment on column settlement_schema.settlement.service_fee is '수수료 합계';

comment on column settlement_schema.settlement.settlement_amount is '실제 정산 금액';

comment on column settlement_schema.settlement.status is '정산 상태';

comment on column settlement_schema.settlement.settled_at is '정산 처리 시각';

comment on column settlement_schema.settlement.created_at is '생성일';

comment on column settlement_schema.settlement.updated_at is '수정일';

alter table settlement_schema.settlement
    owner to postgres;
