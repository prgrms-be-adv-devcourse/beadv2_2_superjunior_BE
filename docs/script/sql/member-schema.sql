create schema member_schema;

set search_path to member_schema;

create table member_schema.member
(
    member_id     uuid                                                           not null
        constraint member_pk
            primary key,
    email         varchar(100)                                                   not null
        constraint member_pk_2
            unique,
    name          varchar(100)                                                   not null
            unique,
    password      varchar(60)                                                    not null,
    role          varchar(20)                                                    not null,
    salt_key      varchar(32)                                                    not null,
    image_url     varchar(2048),
    created_at    timestamp with time zone default now()                         not null,
    updated_at    timestamp with time zone,
    deleted_at    timestamp with time zone,
    phone_number  varchar(20)
);

comment on table member_schema.member is '모든 회원';

comment on column member_schema.member.member_id is '멤버 ID';

comment on column member_schema.member.email is '이메일 주소';

comment on column member_schema.member.name is '이름';

comment on column member_schema.member.password is '비밀번호';

comment on column member_schema.member.role is '멤버 역할';

comment on column member_schema.member.salt_key is '비밀번호용 키';

comment on column member_schema.member.image_url is '프로필 이미지 URL';

comment on column member_schema.member.created_at is '등록일';

comment on column member_schema.member.updated_at is '수정일';

comment on column member_schema.member.deleted_at is '탈퇴일';

comment on column member_schema.member.phone_number is '핸드폰 번호';

alter table member_schema.member
    owner to postgres;

create table member_schema.seller
(
    seller_id                    uuid        not null
        constraint seller_pk
            primary key
        constraint seller_member_id_fk
            references member_schema.member,
    created_at    timestamp with time zone default now()                         not null,
    updated_at                   timestamp with time zone,
    account_number               varchar(20) not null
        constraint seller_pk_3
            unique,
    bank_code                    varchar(20) not null,
    account_holder               varchar(50) not null,
    business_registration_number varchar(15) not null
        constraint seller_pk_2
            unique
);

comment on table member_schema.seller is '판매자';

comment on column member_schema.seller.seller_id is '판매자 ID';

comment on column member_schema.seller.created_at is '판매자 등록일';

comment on column member_schema.seller.updated_at is '수정일';

comment on column member_schema.seller.account_number is '계좌 번호';

comment on column member_schema.seller.bank_code is '은행 코드';

comment on column member_schema.seller.account_holder is '예금주 이름';

comment on column member_schema.seller.business_registration_number is '사업자 등록 번호';

alter table member_schema.seller
    owner to postgres;

create table member_schema.address
(
    address_id     uuid         not null
        constraint address_pk
            primary key,
    address        varchar(100) not null,
    address_detail varchar(100) not null,
    postal_code    varchar(5)  not null,
    receiver_name  varchar(100),
    phone_number  varchar(20),
    member_id      uuid         not null
        constraint address_member_id_fk
            references member_schema.member,
    created_at    timestamp with time zone default now()                         not null,
    updated_at     timestamp with time zone
);

comment on table member_schema.address is '회원 주소';

comment on column member_schema.address.address_id is '주소 ID';

comment on column member_schema.address.address is '주소';

comment on column member_schema.address.address_detail is '상세 주소';

comment on column member_schema.address.postal_code is '우편 번호';

comment on column member_schema.address.receiver_name is '수신자 이름';

comment on column member_schema.address.member_id is '멤버 ID';

comment on column member_schema.address.updated_at is '수정일';

alter table member_schema.address
    owner to postgres;

create table member_schema.email_token
(
    email_token_id uuid                         not null
        constraint email_token_pk
            primary key,
    email          varchar(100)                 not null,
    token          varchar(100)                 not null
        constraint email_token_token_uk
            unique,
    is_verified       boolean                      not null,
    expired_at     timestamp with time zone     not null,
    created_at     timestamp with time zone default now() not null,
    updated_at     timestamp with time zone
);

comment on table member_schema.email_token is '이메일 인증 토큰';

  comment on column member_schema.email_token.email_token_id is '이메일 토큰 ID';

  comment on column member_schema.email_token.email is '이메일 주소';

  comment on column member_schema.email_token.token is '토큰 값';

  comment on column member_schema.email_token.is_verified is '유효 여부';

  comment on column member_schema.email_token.expired_at is '만료 시각';

  comment on column member_schema.email_token.created_at is '생성 시각';

  comment on column member_schema.email_token.updated_at is '수정 시각';

alter table member_schema.email_token
    owner to postgres;
