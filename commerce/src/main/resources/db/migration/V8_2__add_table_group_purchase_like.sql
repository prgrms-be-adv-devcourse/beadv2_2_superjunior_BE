CREATE TABLE product_schema.group_purchase_like
(
    group_purchase_like_id UUID NOT NULL,
    member_id              UUID NOT NULL,
    group_purchase_id      UUID NOT NULL,
    created_at             TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_group_purchase_like PRIMARY KEY (group_purchase_like_id)
);