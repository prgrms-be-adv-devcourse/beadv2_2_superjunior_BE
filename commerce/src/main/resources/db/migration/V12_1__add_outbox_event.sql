CREATE TABLE product_schema.outbox_event
(
    id                    UUID         NOT NULL,
    topic                 VARCHAR(255) NOT NULL,
    message_key           VARCHAR(255) NOT NULL,
    payload_type          VARCHAR(255) NOT NULL,
    payload               JSONB        NOT NULL,
    status                VARCHAR(255) NOT NULL,
    aggregate_type        VARCHAR(255),
    aggregate_id          VARCHAR(255),
    event_type            VARCHAR(255),
    retry_count           INTEGER      NOT NULL,
    created_at            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    published_at          TIMESTAMP WITHOUT TIME ZONE,
    next_attempt_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    processing_started_at TIMESTAMP WITHOUT TIME ZONE,
    last_error            VARCHAR(255),
    CONSTRAINT pk_outbox_event PRIMARY KEY (id)
);

CREATE INDEX idx_outbox_processing_started ON outbox_event (status, processing_started_at);

CREATE INDEX idx_outbox_status_next_attempt ON outbox_event (status, next_attempt_at, created_at);