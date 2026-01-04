-- Enable Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create Schema
CREATE SCHEMA IF NOT EXISTS psp;

-- 1. Payment Partner Meta (설정 정보 유지)
CREATE TABLE IF NOT EXISTS psp.payment_partner_meta (
    id              VARCHAR(36) PRIMARY KEY,
    provider        VARCHAR(50) NOT NULL,
    client_id       VARCHAR(255),
    client_secret   VARCHAR(255),
    merchant_cid    VARCHAR(255),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 기존 테이블이 있다면 삭제 (개발 편의성, 불변 스키마로 전환 위함)
DROP TABLE IF EXISTS psp.payment_methods;
DROP TABLE IF EXISTS psp.payment_transactions; -- 기존에 없었더라도 안전하게
DROP TABLE IF EXISTS psp.payments;

-- 2. Payments (Immutable Ledger)
-- 결제 상태 변경이 아닌, 모든 이벤트(준비, 승인, 취소)가 새로운 행으로 기록됨
CREATE TABLE psp.payments (
    id                  VARCHAR(100) PRIMARY KEY, -- UUID
    merchant_order_id   VARCHAR(100) NOT NULL,    -- 가맹점 주문번호 (그룹핑 키)
    org_payment_id      VARCHAR(100),             -- 원본 결제 ID (취소 시 승인 건 참조, 승인 시 준비 건 참조 등)
    
    -- Transaction Info
    type                VARCHAR(20) NOT NULL,     -- PREPARE, APPROVE, CANCEL
    status              VARCHAR(20) NOT NULL,     -- SUCCESS, FAIL, UNKNOWN
    
    -- PG Info
    pg_provider         VARCHAR(20) NOT NULL DEFAULT 'KAKAOPAY',
    pg_tid              VARCHAR(100),             -- PG사 트랜잭션 ID
    pg_token            VARCHAR(255),             -- 승인 토큰 (PREPARE 단계 저장)
    
    -- Amount
    amount              BIGINT NOT NULL,
    tax_free_amount     BIGINT DEFAULT 0,
    currency            VARCHAR(3) NOT NULL DEFAULT 'KRW',
    
    -- Payment Method Info (역정규화 - 승인 시 저장)
    payment_method_type VARCHAR(20),              -- CARD, MONEY
    card_issuer         VARCHAR(50),
    card_number_masked  VARCHAR(20),
    installments        INTEGER DEFAULT 0,
    
    -- Metadata
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payments_merchant_order_id ON psp.payments(merchant_order_id);
CREATE INDEX idx_payments_pg_tid ON psp.payments(pg_tid);
CREATE INDEX idx_payments_created_at ON psp.payments(created_at);

-- 3. External Payment Transactions (Raw Log)
-- 카카오페이 등 외부 시스템과의 통신 원본 저장
CREATE TABLE psp.external_payment_transactions (
    id                  BIGSERIAL PRIMARY KEY,
    payment_id          VARCHAR(100) NOT NULL, -- FK Removed for performance/flexibility, logically linked to psp.payments(id)
    
    -- Request/Response Details
    provider            VARCHAR(50) NOT NULL,
    url                 VARCHAR(1024) NOT NULL,
    method              VARCHAR(10) NOT NULL,
    request_header      TEXT,
    request_body        JSONB,
    response_body       JSONB,
    response_status     INTEGER,
    
    -- Metadata
    latency_ms          BIGINT,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ext_trans_payment_id ON psp.external_payment_transactions(payment_id);
CREATE INDEX idx_ext_trans_created_at ON psp.external_payment_transactions(created_at);
