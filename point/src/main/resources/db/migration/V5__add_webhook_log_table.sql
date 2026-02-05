CREATE TABLE payment_schema.webhook_log
(
    id            UUID                                      NOT NULL,
    url           VARCHAR(2048)                             NOT NULL,
    event_type    VARCHAR(50)                               NOT NULL,
    payload       TEXT                                      NOT NULL,
    status        VARCHAR(20)                               NOT NULL,
    retry_count   INTEGER                     DEFAULT 0     NOT NULL,
    response_code INTEGER,
    response_body TEXT,
    created_at    TIMESTAMP WITH TIME ZONE DEFAULT NOW()    NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_webhook_log PRIMARY KEY (id)
);
