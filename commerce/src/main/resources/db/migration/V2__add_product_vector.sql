CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE product_schema.product_vector
(
    product_id    UUID    NOT NULL,
    vector        vector(1536) NOT NULL,
    model_version VARCHAR(100),
    updated_at    TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_product_vector PRIMARY KEY (product_id)
);
