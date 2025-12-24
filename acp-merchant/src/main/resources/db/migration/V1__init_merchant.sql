-- Enable Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Products (OpenAI Product Feed Spec)
CREATE TABLE products (
    id              VARCHAR(255) PRIMARY KEY, -- SKU or UUID
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    link            TEXT NOT NULL,
    image_link      TEXT NOT NULL,
    price_amount    NUMERIC(19, 4) NOT NULL,
    currency        VARCHAR(3) NOT NULL DEFAULT 'KRW',
    availability    VARCHAR(50) NOT NULL,
    inventory_qty   INTEGER NOT NULL DEFAULT 0,
    condition       VARCHAR(50) NOT NULL DEFAULT 'new',
    category        VARCHAR(255),
    brand           VARCHAR(255),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 2. Orders (Merchant View)
CREATE TABLE orders (
    id              VARCHAR(36) PRIMARY KEY, -- UUID
    user_id         VARCHAR(255) NOT NULL,
    status          VARCHAR(50) NOT NULL, -- CREATED, AUTHORIZED, COMPLETED, CANCELED
    total_amount    NUMERIC(19, 4) NOT NULL,
    currency        VARCHAR(3) NOT NULL DEFAULT 'KRW',
    payment_request_id VARCHAR(36), -- Reference to PSP's payment ID (Virtual FK)
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE order_lines (
    id              VARCHAR(36) PRIMARY KEY,
    order_id        VARCHAR(36) NOT NULL REFERENCES orders(id),
    product_id      VARCHAR(255) NOT NULL REFERENCES products(id),
    quantity        INTEGER NOT NULL,
    unit_price      NUMERIC(19, 4) NOT NULL,
    currency        VARCHAR(3) NOT NULL DEFAULT 'KRW',
    total_price     NUMERIC(19, 4) NOT NULL
);

CREATE INDEX idx_orders_user ON orders(user_id);
