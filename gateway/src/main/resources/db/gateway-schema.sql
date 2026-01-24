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
('DELETE', '^/api/members/address/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$', 'CONSUMER,SELLER'),
('GET', '^/api/members/addresses$', 'CONSUMER,SELLER'),
('GET', '^/api/members/email/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$', 'GUEST'),
('GET', '^/api/members/email/verification/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$', 'GUEST'),
('GET', '^/api/members/name/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$', 'GUEST'),
('PUT', '^/api/members/password$', 'CONSUMER,SELLER'),
('PUT', '^/api/members/profile$', 'CONSUMER,SELLER'),
('GET', '^/api/members/profile$', 'CONSUMER,SELLER'),
('PUT', '^/api/members/seller$', 'SELLER'),
('POST', '^/api/members/seller$', 'CONSUMER'),
('GET', '^/api/members/seller/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$', 'GUEST,CONSUMER,SELLER'),
('GET', '^/api/members/role$', 'GUEST,CONSUMER,SELLER'),
-- Notifications
('GET', '^/api/notifications$', 'CONSUMER,SELLER'),
('PATCH', '^/api/notifications/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}/read$', 'CONSUMER,SELLER'),
('PATCH', '^/api/notifications/read$', 'CONSUMER,SELLER'),
('GET', '^/api/notifications/unread$', 'CONSUMER,SELLER'),
-- Orders
('POST', '^/api/orders$', 'CONSUMER,SELLER'),
('GET', '^/api/orders/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$', 'CONSUMER,SELLER'),
('POST', '^/api/orders/cart$', 'CONSUMER,SELLER'),
('GET', '^/api/orders/consumer$', 'CONSUMER,SELLER'),
('GET', '^/api/orders/seller$', 'SELLER'),
('GET', '/api/orders/cancel$', 'CONSUMER, SELLER'),
('POST', '^/api/orders/cancel/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$', 'CONSUMER,SELLER'),
-- Payments
('GET', '^/api/payments$', 'CONSUMER,SELLER'),
('GET', '^/api/payments/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$', 'CONSUMER,SELLER'),
('POST', '^/api/payments/confirm$', 'CONSUMER,SELLER'),
('POST', '^/api/payments/fail$', 'CONSUMER,SELLER'),
('POST', '^/api/payments/create$', 'CONSUMER,SELLER'),
-- Points
('GET', '^/api/points$', 'CONSUMER,SELLER'),
('POST', '^/api/points/charge$', 'CONSUMER,SELLER'),
('POST', '^/api/points/deduct$', 'CONSUMER,SELLER'),
('GET',  '^/api/points/histories$', 'CONSUMER,SELLER'),
('POST', '^/api/points/transfer$', 'CONSUMER,SELLER'),
-- Products
('POST', '^/api/products$', 'SELLER'),
('GET', '^/api/products$', 'SELLER'),
('GET', '^/api/products/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$', 'GUEST,CONSUMER,SELLER'),
('DELETE', '^/api/products/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$', 'SELLER'),
('PATCH', '^/api/products/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$', 'SELLER'),
-- Purchases
('GET', '^/api/purchases$', 'GUEST,CONSUMER,SELLER'),
('POST', '^/api/purchases$', 'SELLER'),
('DELETE', '^/api/purchases/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$', 'SELLER'),
('GET', '^/api/purchases/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$', 'GUEST,CONSUMER,SELLER'),
('PATCH', '^/api/purchases/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$', 'SELLER'),
('GET', '^/api/purchases/seller/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$', 'GUEST,CONSUMER,SELLER'),
-- Searches
('GET', '^/api/searches/purchase/search$', 'GUEST, CONSUMER, SELLER'),
('GET', '^/api/searches/purchase/mine$', 'SELLER'),
-- AI
('GET', '^/api/ai/recommandations$', 'CONSUMER,SELLER'),
-- Notification settings
('GET', '^/api/notification-settings$', 'CONSUMER,SELLER'),
('PUT', '^/api/notification-settings$', 'CONSUMER,SELLER');
