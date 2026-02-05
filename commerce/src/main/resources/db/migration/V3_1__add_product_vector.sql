CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE product_vector
(
    product_id    UUID         NOT NULL,
    vector        VECTOR(1536) NOT NULL,
    model_version VARCHAR(255),
    dimension_size     INTEGER,
    updated_at    TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_product_vector PRIMARY KEY (product_id)
);
