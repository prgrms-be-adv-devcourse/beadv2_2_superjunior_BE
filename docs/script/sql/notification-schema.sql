CREATE SCHEMA IF NOT EXISTS notification_schema;

DROP TABLE IF EXISTS notification_schema.notification;

CREATE TABLE IF NOT EXISTS notification_schema.notification
(
    notification_id   UUID                        NOT NULL,
    member_id         UUID                        NOT NULL,
    notification_type VARCHAR(20)                 NOT NULL,
    channel           VARCHAR(20)                 NOT NULL,
    title             VARCHAR(50)                 NOT NULL,
    message           TEXT                        NOT NULL,
    reference_type    VARCHAR(30)                 NOT NULL,
    failure_message   TEXT,
    status            VARCHAR(20)                 NOT NULL,
    created_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at        TIMESTAMP WITHOUT TIME ZONE,
    reference_id      UUID                        NOT NULL,
    CONSTRAINT pk_notification PRIMARY KEY (notification_id)
);
