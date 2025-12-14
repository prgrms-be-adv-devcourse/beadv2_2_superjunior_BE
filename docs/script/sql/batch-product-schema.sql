create schema batch_product_schema;

create table batch_product_schema.batch_job_instance
(
    job_instance_id bigint       not null
        constraint batch_job_instance_pk
            primary key,
    version         bigint,
    job_name        varchar(100) not null,
    job_key         varchar(32)  not null,
    constraint job_inst_un unique (job_name, job_key)
);

alter table batch_product_schema.batch_job_instance
    owner to postgres;

create table batch_product_schema.batch_job_execution
(
    job_execution_id bigint    not null
        constraint batch_job_execution_pk
            primary key,
    version          bigint,
    job_instance_id  bigint    not null
        constraint job_inst_exec_fk
            references batch_product_schema.batch_job_instance,
    create_time      timestamp not null,
    start_time       timestamp default null,
    end_time         timestamp default null,
    status           varchar(10),
    exit_code        varchar(2500),
    exit_message     varchar(2500),
    last_updated     timestamp
);

alter table batch_product_schema.batch_job_execution
    owner to postgres;

create table batch_product_schema.batch_job_execution_params
(
    job_execution_id bigint       not null
        constraint job_exec_params_fk
            references batch_product_schema.batch_job_execution,
    parameter_name   varchar(100) not null,
    parameter_type   varchar(100) not null,
    parameter_value  varchar(2500),
    identifying      char(1)      not null
);

alter table batch_product_schema.batch_job_execution_params
    owner to postgres;

create table batch_product_schema.batch_step_execution
(
    step_execution_id  bigint       not null
        constraint batch_step_execution_pk
            primary key,
    version            bigint       not null,
    step_name          varchar(100) not null,
    job_execution_id   bigint       not null
        constraint job_exec_step_fk
            references batch_product_schema.batch_job_execution,
    create_time        timestamp    not null,
    start_time         timestamp default null,
    end_time           timestamp default null,
    status             varchar(10),
    commit_count       bigint,
    read_count         bigint,
    filter_count       bigint,
    write_count        bigint,
    read_skip_count    bigint,
    write_skip_count   bigint,
    process_skip_count bigint,
    rollback_count     bigint,
    exit_code          varchar(2500),
    exit_message       varchar(2500),
    last_updated       timestamp
);

alter table batch_product_schema.batch_step_execution
    owner to postgres;

create table batch_product_schema.batch_step_execution_context
(
    step_execution_id  bigint        not null
        constraint batch_step_execution_context_pk
            primary key
        constraint step_exec_ctx_fk
            references batch_product_schema.batch_step_execution,
    short_context      varchar(2500) not null,
    serialized_context text
);

alter table batch_product_schema.batch_step_execution_context
    owner to postgres;

create table batch_product_schema.batch_job_execution_context
(
    job_execution_id   bigint        not null
        constraint batch_job_execution_context_pk
            primary key
        constraint job_exec_ctx_fk
            references batch_product_schema.batch_job_execution,
    short_context      varchar(2500) not null,
    serialized_context text
);

alter table batch_product_schema.batch_job_execution_context
    owner to postgres;

create sequence batch_product_schema.batch_step_execution_seq maxvalue 9223372036854775807 no cycle;
create sequence batch_product_schema.batch_job_execution_seq maxvalue 9223372036854775807 no cycle;
create sequence batch_product_schema.batch_job_seq maxvalue 9223372036854775807 no cycle;
