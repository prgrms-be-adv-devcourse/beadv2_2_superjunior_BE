CREATE TABLE notification_schema.notification_setting
(
    setting_id UUID        NOT NULL,
    member_id  UUID        NOT NULL,
    channel    VARCHAR(20) NOT NULL,
    is_enabled BOOLEAN     NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_notification_setting PRIMARY KEY (setting_id)
);

ALTER TABLE notification_schema.notification_setting
    ADD CONSTRAINT uc_617ed66f663178ac699a33387 UNIQUE (member_id, channel);

ALTER TABLE notification_schema.notification
    ALTER COLUMN notification_type TYPE VARCHAR(50) USING (notification_type::VARCHAR(50));

ALTER TABLE member_schema.member
    ALTER COLUMN status TYPE VARCHAR(255) USING (status::VARCHAR(255));

ALTER TABLE member_schema.member
    ALTER COLUMN status DROP NOT NULL;

ALTER TABLE member_schema.seller
    ALTER COLUMN status TYPE VARCHAR(255) USING (status::VARCHAR(255));

ALTER TABLE member_schema.seller
    ALTER COLUMN status DROP NOT NULL;

ALTER TABLE member_schema.email_token
    ALTER COLUMN updated_at DROP NOT NULL;
