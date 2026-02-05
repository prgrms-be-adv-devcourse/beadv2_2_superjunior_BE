CREATE TABLE payment_schema.bonus_earning
(
    id               UUID                        NOT NULL,
    member_id        UUID                        NOT NULL,
    amount           BIGINT                      NOT NULL,
    remaining_amount BIGINT                      NOT NULL,
    type             VARCHAR(255)                NOT NULL,
    policy_id        UUID,
    order_id         UUID,
    reference_id     UUID,
    description      VARCHAR(255),
    expires_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status           VARCHAR(255)                NOT NULL,
    earned_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    used_at          TIMESTAMP WITHOUT TIME ZONE,
    expired_at       TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_bonus_earning PRIMARY KEY (id)
);

CREATE TABLE payment_schema.bonus_policy
(
    id                       UUID         NOT NULL,
    name                     VARCHAR(255) NOT NULL,
    type                     VARCHAR(255) NOT NULL,
    reward_rate              DECIMAL,
    fixed_amount             BIGINT,
    min_purchase_amount      BIGINT,
    max_reward_amount        BIGINT,
    valid_from               TIMESTAMP WITHOUT TIME ZONE,
    valid_until              TIMESTAMP WITHOUT TIME ZONE,
    expiration_days          INTEGER      NOT NULL,
    target_group_purchase_id UUID,
    target_category          VARCHAR(255),
    is_active                BOOLEAN      NOT NULL,
    CONSTRAINT pk_bonus_policy PRIMARY KEY (id)
);

ALTER TABLE payment_schema.bonus_policy
    ADD CONSTRAINT uc_bonus_policy_name UNIQUE (name);
