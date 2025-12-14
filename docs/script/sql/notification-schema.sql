create schema notification_schema;

create table notification_schema.notification
(
    notification_id   uuid        not null
        constraint pk_notification
            primary key,
    member_id         uuid        not null,
    notification_type varchar(40) not null,
    channel           varchar(20) not null,
    title             varchar(50) not null,
    message           text        not null,
    reference_type    varchar(30) not null,
    failure_message   text,
    status            varchar(20) not null,
    created_at        timestamp   not null,
    updated_at        timestamp,
    reference_id      uuid        not null
);

alter table notification_schema.notification
    owner to postgres;
