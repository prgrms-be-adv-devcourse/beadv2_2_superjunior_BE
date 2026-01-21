CREATE EXTENSION IF NOT EXISTS vector SCHEMA public;

CREATE TABLE ai_schema.personal_vector
(
    member_id  UUID NOT NULL,
    vector     VECTOR(1536) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_personal_vector PRIMARY KEY (member_id)
);
