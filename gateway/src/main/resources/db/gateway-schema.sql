CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE SCHEMA gateway_schema;

SET search_path TO gateway_schema;

CREATE TABLE gateway_route (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    http_method varchar(10),
    endpoint varchar(255),
    roles varchar(100),
    CONSTRAINT gateway_route_un UNIQUE (http_method, endpoint)
);

INSERT INTO gateway_route (http_method, endpoint, roles)
VALUES
-- Auth
-- ('POST', '/api/auth/login$', 'GUEST'),
-- ('GET', '/api/auth/logout$', 'CONSUMER,SELLER'),
-- ('GET', '/api/auth/refresh$', 'CONSUMER,SELLER'),
-- Balances
('GET', '^/api/balances$', 'SELLER'),
('GET', '^/api/balances/history$', 'SELLER'),
-- Carts
('POST', '^/api/carts$', 'CONSUMER,SELLER'),
('GET', '^/api/carts$', 'CONSUMER,SELLER'),
('PATCH', '^/api/carts$', 'CONSUMER,SELLER'),
('DELETE', '^/api/carts$', 'CONSUMER,SELLER'),
('DELETE', '^/api/carts/all$', 'CONSUMER,SELLER'),
-- Members
('DELETE', '^/api/members$', 'CONSUMER,SELLER'),
('POST', '^/api/members$', 'GUEST'),
('POST', '^/api/members/address$', 'CONSUMER,SELLER'),
('DELETE', '^/api/members/address/[^/]+$', 'CONSUMER,SELLER'),
('GET', '^/api/members/addresses$', 'CONSUMER,SELLER'),
('GET', '^/api/members/email/[^/]+$', 'GUEST'),
('GET', '^/api/members/email/verification/[^/]+$', 'GUEST'),
('GET', '^/api/members/name/[^/]+$', 'GUEST'),
('PUT', '^/api/members/password$', 'CONSUMER,SELLER'),
('PUT', '^/api/members/profile$', 'CONSUMER,SELLER'),
('GET', '^/api/members/profile$', 'CONSUMER,SELLER'),
('PUT', '^/api/members/seller$', 'SELLER'),
('POST', '^/api/members/seller$', 'CONSUMER'),
('GET', '^/api/members/seller/[^/]+$', 'GUEST,CONSUMER,SELLER'),
-- Notifications
('GET', '^/api/notifications$', 'CONSUMER,SELLER'),
('PATCH', '^/api/notifications/[^/]+/read$', 'CONSUMER,SELLER'),
('PATCH', '^/api/notifications/read$', 'CONSUMER,SELLER'),
('GET', '^/api/notifications/unread$', 'CONSUMER,SELLER'),
-- Orders
('POST', '^/api/orders$', 'CONSUMER,SELLER'),
('GET', '^/api/orders/[^/]+$', 'CONSUMER,SELLER'),
('POST', '^/api/orders/cart$', 'CONSUMER,SELLER'),
('GET', '^/api/orders/consumer$', 'CONSUMER,SELLER'),
('GET', '^/api/orders/seller$', 'SELLER'),
-- Payments
('GET', '^/api/payments$', 'CONSUMER,SELLER'),
('GET', '^/api/payments/[^/]+$', 'CONSUMER,SELLER'),
('GET', '^/api/payments/confirm$', 'CONSUMER,SELLER'),
('POST', '^/api/payments/create$', 'CONSUMER,SELLER'),
('GET', '^/api/payments/refund$', 'CONSUMER,SELLER'),
-- Points
('GET', '^/api/points$', 'CONSUMER,SELLER'),
-- Products
('POST', '^/api/products$', 'SELLER'),
('GET', '^/api/products$', 'SELLER'),
('GET', '^/api/products/[^/]+$', 'GUEST,CONSUMER,SELLER'),
('DELETE', '^/api/products/[^/]+$', 'SELLER'),
('PATCH', '^/api/products/[^/]+$', 'SELLER'),
-- Purchases
('GET', '^/api/purchases$', 'GUEST,CONSUMER,SELLER'),
('POST', '^/api/purchases$', 'SELLER'),
('DELETE', '^/api/purchases/[^/]+$', 'SELLER'),
('GET', '^/api/purchases/[^/]+$', 'GUEST,CONSUMER,SELLER'),
('PATCH', '^/api/purchases/[^/]+$', 'SELLER'),
('GET', '^/api/purchases/seller/[^/]+$', 'GUEST,CONSUMER,SELLER'),
-- Searches
('GET', '^/api/searches/purchase/search$', 'GUEST, CONSUMER, SELLER'),

-- 20260122
-- AI
('GET', '^/api/ai/recommandations$', 'CONSUMER,SELLER'),
-- Members
('GET', '^/api/members/role$', 'GUEST,CONSUMER,SELLER'),
-- Orders
('POST', '^/api/orders/cancel/[^/]+$', 'CONSUMER,SELLER'),
-- Points
('POST', '^/api/points/charge$', 'CONSUMER,SELLER'),
('POST', '^/api/points/deduct$', 'CONSUMER,SELLER'),
('GET',  '^/api/points/histories$', 'CONSUMER,SELLER'),
('POST', '^/api/points/transfer$', 'CONSUMER,SELLER'),
-- Notification settings
('GET', '^/api/notification-settings$', 'CONSUMER,SELLER'),
('PUT', '^/api/notification-settings$', 'CONSUMER,SELLER');
