create schema product_schema;

create table product_schema.product
(
    product_id   uuid                                   not null
        constraint product_pk
            primary key,
    name         varchar(100)                           not null,
    price        integer                                not null,
    category     varchar(20)                            not null
        constraint category_check
            check ((category)::text = ANY
                   (ARRAY [('HOME'::character varying)::text, ('FOOD'::character varying)::text, ('HEALTH'::character varying)::text, ('BEAUTY'::character varying)::text, ('FASHION'::character varying)::text, ('ELECTRONICS'::character varying)::text, ('KIDS'::character varying)::text, ('HOBBY'::character varying)::text, ('PET'::character varying)::text])),
    description  text                                   not null,
    stock        integer                  default 0     not null,
    original_url varchar(255)                           null,
    seller_id    uuid                                   not null,
    created_at   timestamp with time zone default now() not null,
    updated_at   timestamp with time zone,
    deleted_at   timestamp with time zone
);

comment on table product_schema.product is '상품';

comment on column product_schema.product.product_id is '상품 ID';

comment on column product_schema.product.name is '상품 이름';

comment on column product_schema.product.price is '상품 가격';

comment on column product_schema.product.category is '상품 카테고리';

comment on column product_schema.product.description is '상품 설명';

comment on column product_schema.product.stock is '재고';

comment on column product_schema.product.original_url is '원상품 링크';

comment on column product_schema.product.seller_id is '판매자 ID';

comment on column product_schema.product.created_at is '등록일';

comment on column product_schema.product.updated_at is '수정일';

comment on column product_schema.product.deleted_at is '삭제일';

alter table product_schema.product
    owner to postgres;

create table product_schema.group_purchase
(
    group_purchase_id uuid                                                            not null
        constraint group_purchase_pk
            primary key,
    min_quantity      integer                  default 1                              not null,
    max_quantity      integer,
    title             varchar(100)                                                    not null,
    description       text,
    discounted_price  integer                  default 0                              not null,
    status            varchar(20)              default 'SCHEDULED'::character varying not null
        constraint status_check
            check ((status)::text = ANY
                   (ARRAY [('SCHEDULED'::character varying)::text, ('OPEN'::character varying)::text, ('SUCCESS'::character varying)::text, ('FAILED'::character varying)::text])),
    start_date        timestamp with time zone                                        not null,
    end_date          timestamp with time zone                                        not null,
    seller_id         uuid                                                            not null,
    product_id        uuid                                                            not null
        constraint group_purchase_product_product_id_fk
            references product_schema.product,
    current_quantity  integer                  default 0                              not null,
    version           bigint                   default 0,
    created_at        timestamp with time zone default now()                          not null,
    updated_at        timestamp with time zone,
    settled_at        timestamp with time zone ,
    returned_at        timestamp with time zone
);

comment on table product_schema.group_purchase is '공동 구매';

comment on column product_schema.group_purchase.group_purchase_id is '공동 구매 ID';

comment on column product_schema.group_purchase.min_quantity is '공구 성공 최소 수량';

comment on column product_schema.group_purchase.max_quantity is '공구 참여 최대 수량';

comment on column product_schema.group_purchase.title is '공구 제목';

comment on column product_schema.group_purchase.description is '공구 상세 설명';

comment on column product_schema.group_purchase.discounted_price is '공구 할인 가격';

comment on column product_schema.group_purchase.status is '공구 상태';

comment on column product_schema.group_purchase.start_date is '공구 시작 시간';

comment on column product_schema.group_purchase.end_date is '공구 종료 시간';

comment on column product_schema.group_purchase.seller_id is '판매자 ID';

comment on column product_schema.group_purchase.product_id is '상품 ID';

comment on column product_schema.group_purchase.current_quantity is '현재 주문 수량';

comment on column product_schema.group_purchase.version is '낙관적 락 버전';

comment on column product_schema.group_purchase.created_at is '등록일';

comment on column product_schema.group_purchase.updated_at is '수정일';

comment on column product_schema.group_purchase.settled_at is '정산 완료일';
        
comment on column product_schema.group_purchase.returned_at is '환불 완료일';

alter table product_schema.group_purchase
    owner to postgres;

create table product_schema.image
(
    image_id    uuid                                   not null
        constraint image_pk
            primary key,
    file_name   varchar(255)                           not null,
    file_type   varchar(10)                            not null,
    size        bigint                                 not null,
    file_path   varchar(255)                           not null,
    uploaded_at timestamp with time zone default now() not null
);

comment on table product_schema.image is '상품 이미지';

comment on column product_schema.image.image_id is '이미지 ID';

comment on column product_schema.image.file_name is '파일 이름';

comment on column product_schema.image.file_type is '파일 타입';

comment on column product_schema.image.size is '파일 크기';

comment on column product_schema.image.file_path is '파일 경로';

comment on column product_schema.image.uploaded_at is '업로드일';

alter table product_schema.image
    owner to postgres;
