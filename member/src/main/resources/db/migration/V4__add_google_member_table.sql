CREATE TABLE member_schema.google_member
(
    email VARCHAR(100) NOT NULL,
    name  VARCHAR(100) NOT NULL,
    sub   VARCHAR(255) NOT NULL,
    CONSTRAINT pk_google_member PRIMARY KEY (email)
);

ALTER TABLE member_schema.email_token
    ALTER
    COLUMN token TYPE VARCHAR(6) USING (token::VARCHAR(6));

ALTER TABLE member_schema.email_token
    ADD CONSTRAINT uc_email_token_email UNIQUE (email);

ALTER TABLE member_schema.email_token
    DROP CONSTRAINT IF EXISTS uc_email_token_token;

ALTER TABLE member_schema.member
DROP CONSTRAINT IF EXISTS uc_member_name;
