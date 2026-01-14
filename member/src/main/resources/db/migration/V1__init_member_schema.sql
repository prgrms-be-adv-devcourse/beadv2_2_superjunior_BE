CREATE TABLE notification_schema.notification
(
    notification_id   UUID        NOT NULL,
    member_id         UUID        NOT NULL,
    notification_type VARCHAR(20) NOT NULL,
    channel           VARCHAR(20) NOT NULL,
    title             VARCHAR(50) NOT NULL,
    message           TEXT        NOT NULL,
    reference_type    VARCHAR(30) NOT NULL,
    failure_message   TEXT,
    status            VARCHAR(20) NOT NULL,
    created_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at        TIMESTAMP WITHOUT TIME ZONE,
    reference_id      UUID        NOT NULL,
    CONSTRAINT pk_notification PRIMARY KEY (notification_id)
);

CREATE TABLE member_schema.address
(
    address_id     UUID         NOT NULL,
    member_id      UUID,
    address        VARCHAR(100) NOT NULL,
    address_detail VARCHAR(100) NOT NULL,
    postal_code    VARCHAR(5)   NOT NULL,
    receiver_name  VARCHAR(100),
    phone_number   VARCHAR(20),
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_address PRIMARY KEY (address_id)
);

CREATE TABLE member_schema.email_token
(
    email_token_id UUID         NOT NULL,
    email          VARCHAR(100) NOT NULL,
    token          VARCHAR(100) NOT NULL,
    is_verified    BOOLEAN      NOT NULL,
    expired_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_email_token PRIMARY KEY (email_token_id)
);

CREATE TABLE member_schema.member
(
    member_id    UUID         NOT NULL,
    email        VARCHAR(100) NOT NULL,
    name         VARCHAR(100) NOT NULL,
    password     VARCHAR(60)  NOT NULL,
    phone_number VARCHAR(20),
    role         VARCHAR(20)  NOT NULL,
    salt_key     VARCHAR(32)  NOT NULL,
    image_url    VARCHAR(2048),
    created_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITHOUT TIME ZONE,
    deleted_at   TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_member PRIMARY KEY (member_id)
);

CREATE TABLE member_schema.seller
(
    seller_id                    UUID        NOT NULL,
    created_at                   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at                   TIMESTAMP WITHOUT TIME ZONE,
    bank_code                    VARCHAR(20) NOT NULL,
    account_number               VARCHAR(20) NOT NULL,
    account_holder               VARCHAR(50) NOT NULL,
    business_registration_number VARCHAR(15) NOT NULL,
    CONSTRAINT pk_seller PRIMARY KEY (seller_id)
);

ALTER TABLE member_schema.email_token
    ADD CONSTRAINT uc_email_token_token UNIQUE (token);

ALTER TABLE member_schema.member
    ADD CONSTRAINT uc_member_email UNIQUE (email);

ALTER TABLE member_schema.member
    ADD CONSTRAINT uc_member_name UNIQUE (name);

ALTER TABLE member_schema.seller
    ADD CONSTRAINT uc_seller_business_registration_number UNIQUE (business_registration_number);

ALTER TABLE member_schema.address
    ADD CONSTRAINT FK_ADDRESS_ON_MEMBER FOREIGN KEY (member_id) REFERENCES member_schema.member (member_id);

ALTER TABLE member_schema.seller
    ADD CONSTRAINT FK_SELLER_ON_SELLER FOREIGN KEY (seller_id) REFERENCES member_schema.member (member_id);
