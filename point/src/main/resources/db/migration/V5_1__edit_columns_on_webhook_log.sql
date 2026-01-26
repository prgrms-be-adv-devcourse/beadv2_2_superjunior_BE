ALTER TABLE payment_schema.webhook_log
    ADD error_message TEXT;

ALTER TABLE payment_schema.webhook_log
    ADD occurred_at TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE payment_schema.webhook_log
    ADD sent_at TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE payment_schema.webhook_log
    ADD webhook_id VARCHAR(255);

ALTER TABLE payment_schema.webhook_log
    ALTER COLUMN occurred_at SET NOT NULL;

ALTER TABLE payment_schema.webhook_log
    ALTER COLUMN webhook_id SET NOT NULL;

ALTER TABLE payment_schema.webhook_log
    ADD CONSTRAINT uc_webhook_log_webhook UNIQUE (webhook_id);

ALTER TABLE payment_schema.webhook_log
    DROP COLUMN response_body;

ALTER TABLE payment_schema.webhook_log
    DROP COLUMN response_code;

ALTER TABLE payment_schema.webhook_log
    DROP COLUMN url;
