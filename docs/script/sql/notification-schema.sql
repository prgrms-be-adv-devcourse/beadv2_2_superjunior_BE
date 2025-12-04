create schema notification_schema;

create table notification_schema.notification
(
    notification_id   uuid                                   not null
        constraint notification_pk
            primary key,
    member_id         uuid                                   not null,
    notification_type varchar(20)                            not null
        constraint notification_type_check
            check ((notification_type)::text = ANY
                   (ARRAY [('ORDER_SUCCESS'::character varying)::text, ('SETTLEMENT_FAIL'::character varying)::text])),
    channel           varchar(20)                            not null
        constraint channel_check
            check ((channel)::text = ANY
                   (ARRAY [('KAKAO'::character varying)::text, ('EMAIL'::character varying)::text, ('IN_APP'::character varying)::text])),
    title             varchar(50)                            not null,
    message           text                                   not null,
    reference_type    varchar(30)                            not null,
    failure_message   text,
    status            varchar(20)                            not null
        constraint status_check
            check ((status)::text = ANY
                   (ARRAY [('SENT'::character varying)::text, ('FAILED'::character varying)::text, ('READ'::character varying)::text])),
    created_at        timestamp with time zone default now() not null,
    updated_at        timestamp with time zone,
    reference_id      uuid                                   not null
);

comment on table notification_schema.notification is '알림';

comment on column notification_schema.notification.notification_id is '알림 ID';

comment on column notification_schema.notification.member_id is '멤버 ID';

comment on column notification_schema.notification.notification_type is '알림 종류';

comment on column notification_schema.notification.channel is '알림 채널';

comment on column notification_schema.notification.title is '제목';

comment on column notification_schema.notification.message is '본문';

comment on column notification_schema.notification.reference_type is '참조 타입';

comment on column notification_schema.notification.failure_message is '실패 매시지';

comment on column notification_schema.notification.status is '상태';

comment on column notification_schema.notification.created_at is '생성 시간';

comment on column notification_schema.notification.updated_at is '수정 시간';

comment on column notification_schema.notification.reference_id is '참조 도메인 ID';

alter table notification_schema.notification
    owner to postgres;
