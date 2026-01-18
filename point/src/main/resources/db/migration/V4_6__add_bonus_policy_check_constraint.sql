-- bonus_policy 테이블에 CHECK 제약조건 추가
-- reward_rate와 fixed_amount 중 하나만 설정 가능
-- reward_rate 사용 시 max_reward_amount 필수

ALTER TABLE payment_schema.bonus_policy
    ADD CONSTRAINT ck_bonus_policy_reward_type CHECK (
        (reward_rate IS NOT NULL AND fixed_amount IS NULL AND max_reward_amount IS NOT NULL)
        OR (reward_rate IS NULL AND fixed_amount IS NOT NULL)
    );
