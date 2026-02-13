ALTER TABLE product_schema.group_purchase_like
    ADD CONSTRAINT uc_3d029ab021c18d7dccc661538 UNIQUE (member_id, group_purchase_id);

ALTER TABLE product_schema.group_purchase
DROP
COLUMN version;