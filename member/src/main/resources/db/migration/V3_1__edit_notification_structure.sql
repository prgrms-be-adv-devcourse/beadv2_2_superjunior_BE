ALTER TABLE notification_schema.notification
    ADD CONSTRAINT uk_notification_deduplication UNIQUE (reference_id, notification_type, member_id);

ALTER TABLE notification_schema.notification
    DROP COLUMN reference_type;
