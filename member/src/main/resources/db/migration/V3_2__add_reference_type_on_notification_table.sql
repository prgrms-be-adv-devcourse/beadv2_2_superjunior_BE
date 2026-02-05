ALTER TABLE notification_schema.notification
    ADD reference_type VARCHAR(20);

ALTER TABLE notification_schema.notification
    ALTER COLUMN reference_type SET NOT NULL;
