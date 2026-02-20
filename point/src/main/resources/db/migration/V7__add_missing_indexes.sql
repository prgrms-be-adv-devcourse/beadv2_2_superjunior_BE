CREATE INDEX idx_bonus_earning_member_status ON payment_schema.bonus_earning (member_id, status);

CREATE INDEX idx_pg_payment_cancel_payment ON payment_schema.pg_payment_cancel (payment_id);

CREATE INDEX idx_pg_payment_member ON payment_schema.pg_payment (member_id);

CREATE INDEX idx_point_transaction_member_created ON payment_schema.point_transaction (member_id, created_at DESC);
