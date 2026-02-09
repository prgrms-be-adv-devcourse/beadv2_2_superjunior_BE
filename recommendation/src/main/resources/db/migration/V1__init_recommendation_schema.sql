CREATE EXTENSION IF NOT EXISTS vector SCHEMA public;

CREATE SCHEMA IF NOT EXISTS recommendation_schema;

CREATE TABLE recommendation_schema.personal_vector
(
    member_id  UUID NOT NULL,
    vector     VECTOR(1536) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_personal_vector PRIMARY KEY (member_id)
);

CREATE TABLE recommendation_schema.product_vector
(
    product_id    UUID         NOT NULL,
    vector        VECTOR(1536) NOT NULL,
    model_version VARCHAR(255),
    dimension_size     INTEGER,
    updated_at    TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_product_vector PRIMARY KEY (product_id)
);
