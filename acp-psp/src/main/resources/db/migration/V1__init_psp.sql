-- Enable Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create Schema
CREATE SCHEMA IF NOT EXISTS psp;

-- 1. Payment Partner Meta
CREATE TABLE psp.payment_partner_meta (
    id              VARCHAR(36) PRIMARY KEY,
    provider        VARCHAR(50) NOT NULL,
    client_id       VARCHAR(255),
    client_secret   VARCHAR(255),
    merchant_cid    VARCHAR(255),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 2. Payments (Transaction Master)
CREATE TABLE psp.payments (
    id              VARCHAR(36) PRIMARY KEY, -- PSP Internal ID
    merchant_order_id VARCHAR(36) NOT NULL, -- Reference to Merchant Order
    amount          NUMERIC(19, 4) NOT NULL,
    currency        VARCHAR(3) NOT NULL DEFAULT 'KRW',
    status          VARCHAR(50) NOT NULL, -- READY, IN_PROGRESS, DONE, CANCELED, FAILED
    pg_tid          VARCHAR(255),
    pg_token        VARCHAR(255),
    partner_meta_id VARCHAR(36) REFERENCES psp.payment_partner_meta(id),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    approved_at     TIMESTAMP WITH TIME ZONE,
    failed_at       TIMESTAMP WITH TIME ZONE,
    fail_reason     TEXT
);

-- 3. Payment Methods
CREATE TABLE psp.payment_methods (
    id              VARCHAR(36) PRIMARY KEY,
    payment_id      VARCHAR(36) NOT NULL REFERENCES psp.payments(id),
    type            VARCHAR(50) NOT NULL,
    provider        VARCHAR(50) NOT NULL,
    card_bin        VARCHAR(6),
    card_issuer     VARCHAR(100),
    installments    INTEGER DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payments_merchant_order ON psp.payments(merchant_order_id);
CREATE INDEX idx_payments_pg_tid ON psp.payments(pg_tid);
