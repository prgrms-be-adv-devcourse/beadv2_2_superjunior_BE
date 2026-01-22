-- settlement 테이블 타임스탬프 컬럼 변경
ALTER TABLE settlement_schema.settlement
ALTER COLUMN period_start TYPE TIMESTAMPTZ,
    ALTER COLUMN period_end TYPE TIMESTAMPTZ,
    ALTER COLUMN settled_at TYPE TIMESTAMPTZ,
    ALTER COLUMN created_at TYPE TIMESTAMPTZ,
    ALTER COLUMN updated_at TYPE TIMESTAMPTZ;

-- settlement_failure 테이블 타임스탬프 컬럼 변경
ALTER TABLE settlement_schema.settlement_failure
ALTER COLUMN period_start TYPE TIMESTAMPTZ,
    ALTER COLUMN period_end TYPE TIMESTAMPTZ,
    ALTER COLUMN created_at TYPE TIMESTAMPTZ;