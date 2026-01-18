-- Add status column expected by Member entity
ALTER TABLE member_schema.member
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

-- Add status column expected by Seller entity
ALTER TABLE member_schema.seller
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

-- Ensure existing rows get defaults before constraints rely on them
UPDATE member_schema.member SET status = 'PENDING' WHERE status IS NULL;
UPDATE member_schema.seller SET status = 'PENDING' WHERE status IS NULL;

-- Convert all timestamp columns to TIMESTAMP WITH TIME ZONE (assumes existing values are UTC)
ALTER TABLE notification_schema.notification
ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING created_at AT TIME ZONE 'UTC',
      ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE member_schema.address
ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING created_at AT TIME ZONE 'UTC',
      ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE member_schema.email_token
ALTER COLUMN expired_at TYPE TIMESTAMP WITH TIME ZONE USING expired_at AT TIME ZONE 'UTC',
      ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING created_at AT TIME ZONE 'UTC',
      ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE member_schema.member
ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING created_at AT TIME ZONE 'UTC',
      ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE USING updated_at AT TIME ZONE 'UTC',
      ALTER COLUMN deleted_at TYPE TIMESTAMP WITH TIME ZONE USING deleted_at AT TIME ZONE 'UTC';

ALTER TABLE member_schema.seller
ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING created_at AT TIME ZONE 'UTC',
      ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE USING updated_at AT TIME ZONE 'UTC';
