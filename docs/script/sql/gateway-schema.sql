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
-- member / MemberController
('POST', '/api/members', 'GUEST'),
('DELETE', '/api/members', 'CONSUMER,SELLER'),
('PUT', '/api/members/password', 'CONSUMER,SELLER'),
('GET', '/api/members/profile', 'CONSUMER,SELLER'),
('PUT', '/api/members/profile', 'CONSUMER,SELLER'),
('GET', '/api/members/name/{name}', 'GUEST'),
('GET', '/api/members/email/{email}', 'GUEST'),
('GET', '/api/members/email/verification/{token}', 'GUEST'),
('POST', '/api/members/seller', 'CONSUMER'),
('GET', '/api/members/seller/{sellerId}', 'GUEST,CONSUMER,SELLER'),
('PUT', '/api/members/seller', 'SELLER'),
('POST', '/api/members/address', 'CONSUMER,SELLER'),
('GET', '/api/members/addresses', 'CONSUMER,SELLER'),
('DELETE', '/api/members/address/{addressId}', 'CONSUMER,SELLER'),

-- member / AuthController
('POST', '/api/auth/login', 'GUEST'),
('GET', '/api/auth/refresh', 'CONSUMER,SELLER'),
('GET', '/api/auth/logout', 'CONSUMER,SELLER'),

-- member / NotificationController
('PATCH', '/api/notifications/{id}/read', 'CONSUMER,SELLER'),
('PATCH', '/api/notifications/read', 'CONSUMER,SELLER'),
('GET', '/api/notifications', 'CONSUMER,SELLER'),
('GET', '/api/notifications/unread', 'CONSUMER,SELLER'),

-- order / OrderController
('POST', '/api/orders', 'CONSUMER,SELLER'),
('POST', '/api/orders/cart', 'CONSUMER,SELLER'),
('GET', '/api/orders/{orderId}', 'CONSUMER,SELLER'),
('GET', '/api/orders/consumer', 'CONSUMER,SELLER'),
('GET', '/api/orders/seller', 'SELLER'),

-- order / CartController
('POST', '/api/carts', 'CONSUMER,SELLER'),
('DELETE', '/api/carts', 'CONSUMER,SELLER'),
('PATCH', '/api/carts', 'CONSUMER,SELLER'),
('GET', '/api/carts', 'CONSUMER,SELLER'),
('DELETE', '/api/carts/all', 'CONSUMER,SELLER'),

-- order / SellerBalanceController
('GET', '/api/balances', 'SELLER'),
('GET', '/api/balances/history', 'SELLER'),

-- product / ProductController
('POST', '/api/products', 'SELLER'),
('DELETE', '/api/products/{productId}', 'SELLER'),
('GET', '/api/products/{productId}', 'GUEST,CONSUMER,SELLER'),
('GET', '/api/products', 'SELLER'),
('PATCH', '/api/products/{productId}', 'SELLER'),

-- product / GroupPurchaseController
('POST', '/api/purchases', 'SELLER'),
('GET', '/api/purchases/{purchaseId}', 'GUEST,CONSUMER,SELLER'),
('GET', '/api/purchases', 'GUEST,CONSUMER,SELLER'),
('GET', '/api/purchases/seller/{sellerId}', 'GUEST,CONSUMER,SELLER'),
('DELETE', '/api/purchases/{purchaseId}', 'SELLER'),
('PATCH', '/api/purchases/{purchaseId}', 'SELLER'),

-- point / MemberPointController
('GET', '/api/points', 'CONSUMER,SELLER'),

-- point / PaymentPointController
('POST', '/api/payments/create', 'CONSUMER,SELLER'),
('GET', '/api/payments/confirm', 'CONSUMER,SELLER'),
('GET', '/api/payments/fail', 'CONSUMER,SELLER'),
('POST', '/api/payments/refund', 'CONSUMER,SELLER'),
('GET', '/api/payments', 'CONSUMER,SELLER'),
('GET', '/api/payments/{id}', 'CONSUMER,SELLER'),

-- elastic-search / ProductSearchController
('GET', '/api/searches/product/search', 'SELLER'),

-- elastic-search / GroupPurchaseSearchController
('GET', '/api/searches/purchase/search', 'SELLER'),
('GET', '/api/searches/purchase/search/all', 'GUEST,CONSUMER,SELLER'),
('GET', '/api/searches/purchase/search/seller', 'GUEST,CONSUMER,SELLER');
