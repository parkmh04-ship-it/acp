# 🗄️ 데이터베이스 스키마 설계

> **설계 원칙**: Merchant와 PSP 도메인을 물리적으로 분리하여 각각 독립적인 스키마를 소유하며, jOOQ를 통한 Type-Safe 접근과 정규화된 설계를 통해 데이터 무결성을 보장합니다.

---

## 📐 스키마 개요

### 데이터베이스 구성

```
PostgreSQL 16
├── Schema: merchant    (Merchant 서버 전용)
│   ├── products
│   ├── product_variants
│   ├── product_images
│   ├── orders
│   ├── order_lines
│   ├── checkout_sessions
│   └── checkout_session_items
│
└── Schema: psp         (PSP 서버 전용)
    ├── payments
    ├── payment_transactions
    ├── payment_partner_meta
    └── idempotency_keys
```

### 격리 전략

- **물리적 분리**: 각 서버는 자신의 스키마에만 접근 (DB User 권한 분리)
- **Virtual FK**: 서로 다른 스키마 간 참조는 애플리케이션 레벨에서 관리
  - 예: `orders.payment_request_id` → `payments.id` (Virtual FK)
- **이벤트 기반 동기화**: Webhook을 통해 상태 동기화

---

## 🛒 Merchant 도메인 (Schema: `merchant`)

### 1. `products` - 상품 정보

OpenAI Product Feed Spec을 완벽히 준수하는 상품 마스터 테이블.

```sql
CREATE TABLE merchant.products (
    -- Primary Key
    id                      VARCHAR(100) PRIMARY KEY,
    
    -- Basic Product Data (Required)
    title                   VARCHAR(500) NOT NULL,
    description             TEXT NOT NULL,
    link                    VARCHAR(2048) NOT NULL,
    
    -- Price & Currency (Required)
    price_amount            BIGINT NOT NULL,  -- 최소 단위 (예: 89000원 = 89000)
    currency                VARCHAR(3) NOT NULL DEFAULT 'KRW',  -- ISO 4217
    
    -- Availability (Required)
    availability            VARCHAR(20) NOT NULL,  -- in_stock, out_of_stock, preorder
    stock_quantity          INTEGER NOT NULL DEFAULT 0,
    
    -- Item Information (Recommended)
    gtin                    VARCHAR(50),  -- Global Trade Item Number (바코드)
    mpn                     VARCHAR(100), -- Manufacturer Part Number
    brand                   VARCHAR(100),
    product_category        VARCHAR(200),
    condition               VARCHAR(20) DEFAULT 'new',  -- new, refurbished, used
    
    -- Pricing & Promotions (Optional)
    sale_price_amount       BIGINT,  -- 할인가
    sale_price_start_date   TIMESTAMP,
    sale_price_end_date     TIMESTAMP,
    
    -- Fulfillment (Recommended)
    shipping_weight_grams   INTEGER,  -- 배송 무게 (그램)
    shipping_length_cm      DECIMAL(10, 2),
    shipping_width_cm       DECIMAL(10, 2),
    shipping_height_cm      DECIMAL(10, 2),
    fulfillment_time_min_days INTEGER DEFAULT 1,
    fulfillment_time_max_days INTEGER DEFAULT 3,
    
    -- Merchant Info (Recommended)
    merchant_name           VARCHAR(200),
    merchant_url            VARCHAR(2048),
    
    -- Returns (Recommended)
    return_policy_days      INTEGER DEFAULT 7,
    return_policy_url       VARCHAR(2048),
    
    -- Performance Signals (Optional)
    reviews_average_rating  DECIMAL(3, 2),  -- 0.00 ~ 5.00
    reviews_count           INTEGER DEFAULT 0,
    
    -- Metadata
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at              TIMESTAMP,  -- Soft Delete
    
    -- Constraints
    CONSTRAINT chk_price_positive CHECK (price_amount > 0),
    CONSTRAINT chk_availability CHECK (availability IN ('in_stock', 'out_of_stock', 'preorder')),
    CONSTRAINT chk_condition CHECK (condition IN ('new', 'refurbished', 'used')),
    CONSTRAINT chk_rating_range CHECK (reviews_average_rating >= 0 AND reviews_average_rating <= 5)
);

-- Indexes
CREATE INDEX idx_products_availability ON merchant.products(availability) WHERE deleted_at IS NULL;
CREATE INDEX idx_products_category ON merchant.products(product_category) WHERE deleted_at IS NULL;
CREATE INDEX idx_products_brand ON merchant.products(brand) WHERE deleted_at IS NULL;
CREATE INDEX idx_products_price ON merchant.products(price_amount) WHERE deleted_at IS NULL;
CREATE INDEX idx_products_updated_at ON merchant.products(updated_at DESC);

-- Full-Text Search (상품 검색 최적화)
CREATE INDEX idx_products_fts ON merchant.products 
    USING gin(to_tsvector('korean', title || ' ' || description))
    WHERE deleted_at IS NULL;
```

---

### 2. `product_variants` - 상품 변형 (색상, 사이즈 등)

```sql
CREATE TABLE merchant.product_variants (
    id                      VARCHAR(100) PRIMARY KEY,
    product_id              VARCHAR(100) NOT NULL REFERENCES merchant.products(id) ON DELETE CASCADE,
    
    -- Variant Attributes
    variant_type            VARCHAR(50) NOT NULL,  -- color, size, material
    variant_value           VARCHAR(100) NOT NULL, -- red, XL, cotton
    
    -- Pricing (변형별 가격 차이)
    price_adjustment        BIGINT DEFAULT 0,  -- 기본 가격 대비 차이 (+/-)
    
    -- Availability
    stock_quantity          INTEGER NOT NULL DEFAULT 0,
    sku                     VARCHAR(100) UNIQUE,  -- Stock Keeping Unit
    
    -- Metadata
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    UNIQUE(product_id, variant_type, variant_value)
);

CREATE INDEX idx_variants_product_id ON merchant.product_variants(product_id);
CREATE INDEX idx_variants_sku ON merchant.product_variants(sku);
```

---

### 3. `product_images` - 상품 이미지 (다중 이미지 지원)

```sql
CREATE TABLE merchant.product_images (
    id                      BIGSERIAL PRIMARY KEY,
    product_id              VARCHAR(100) NOT NULL REFERENCES merchant.products(id) ON DELETE CASCADE,
    
    -- Image Info
    image_url               VARCHAR(2048) NOT NULL,
    image_type              VARCHAR(20) NOT NULL,  -- main, additional, thumbnail
    display_order           INTEGER NOT NULL DEFAULT 0,
    
    -- Alt Text (접근성)
    alt_text                VARCHAR(500),
    
    -- Metadata
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_image_type CHECK (image_type IN ('main', 'additional', 'thumbnail'))
);

CREATE INDEX idx_images_product_id ON merchant.product_images(product_id, display_order);
```

---

### 4. `orders` - 주문 정보

```sql
CREATE TABLE merchant.orders (
    -- Primary Key
    id                      VARCHAR(100) PRIMARY KEY,
    
    -- Buyer Information
    user_id                 VARCHAR(100) NOT NULL,
    buyer_email             VARCHAR(255),
    buyer_name              VARCHAR(200),
    
    -- Order Status
    status                  VARCHAR(20) NOT NULL,  -- PENDING, AUTHORIZED, COMPLETED, CANCELED, FAILED
    
    -- Pricing (최소 단위, 예: 원)
    items_base_amount       BIGINT NOT NULL,  -- 상품 기본 금액 합계
    items_discount_amount   BIGINT DEFAULT 0,
    subtotal_amount         BIGINT NOT NULL,  -- items_base - items_discount
    fulfillment_amount      BIGINT DEFAULT 0,  -- 배송비
    tax_amount              BIGINT DEFAULT 0,
    total_amount            BIGINT NOT NULL,   -- subtotal + fulfillment + tax
    currency                VARCHAR(3) NOT NULL DEFAULT 'KRW',
    
    -- Fulfillment Address
    fulfillment_name        VARCHAR(200),
    fulfillment_phone       VARCHAR(50),
    fulfillment_line_one    VARCHAR(500),
    fulfillment_line_two    VARCHAR(500),
    fulfillment_city        VARCHAR(100),
    fulfillment_state       VARCHAR(100),
    fulfillment_country     VARCHAR(2),  -- ISO 3166-1 alpha-2
    fulfillment_postal_code VARCHAR(20),
    
    -- Payment Reference (Virtual FK to psp.payments)
    payment_request_id      VARCHAR(100),  -- PSP의 payment.id
    
    -- Checkout Session Reference
    checkout_session_id     VARCHAR(100),
    
    -- Timestamps
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    authorized_at           TIMESTAMP,  -- 결제 승인 시각
    completed_at            TIMESTAMP,  -- 배송 완료 시각
    canceled_at             TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_order_status CHECK (status IN ('PENDING', 'AUTHORIZED', 'COMPLETED', 'CANCELED', 'FAILED')),
    CONSTRAINT chk_total_positive CHECK (total_amount > 0)
);

-- Indexes
CREATE INDEX idx_orders_user_id ON merchant.orders(user_id, created_at DESC);
CREATE INDEX idx_orders_status ON merchant.orders(status, created_at DESC);
CREATE INDEX idx_orders_payment_id ON merchant.orders(payment_request_id);
CREATE INDEX idx_orders_checkout_session ON merchant.orders(checkout_session_id);
CREATE INDEX idx_orders_created_at ON merchant.orders(created_at DESC);

-- Trigger: updated_at 자동 갱신
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_orders_updated_at
    BEFORE UPDATE ON merchant.orders
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
```

---

### 5. `order_lines` - 주문 상품 항목

```sql
CREATE TABLE merchant.order_lines (
    id                      BIGSERIAL PRIMARY KEY,
    order_id                VARCHAR(100) NOT NULL REFERENCES merchant.orders(id) ON DELETE CASCADE,
    
    -- Product Reference (구매 시점 스냅샷)
    product_id              VARCHAR(100) NOT NULL,
    product_title           VARCHAR(500) NOT NULL,  -- 스냅샷 (상품명 변경 대비)
    product_image_url       VARCHAR(2048),
    
    -- Variant (선택 사항)
    variant_id              VARCHAR(100),
    variant_description     VARCHAR(200),  -- "색상: 빨강, 사이즈: XL"
    
    -- Quantity & Pricing
    quantity                INTEGER NOT NULL,
    unit_price_amount       BIGINT NOT NULL,  -- 구매 시점 단가
    discount_amount         BIGINT DEFAULT 0,
    subtotal_amount         BIGINT NOT NULL,  -- unit_price * quantity - discount
    tax_amount              BIGINT DEFAULT 0,
    total_amount            BIGINT NOT NULL,  -- subtotal + tax
    currency                VARCHAR(3) NOT NULL DEFAULT 'KRW',
    
    -- Metadata
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_quantity_positive CHECK (quantity > 0),
    CONSTRAINT chk_unit_price_positive CHECK (unit_price_amount > 0)
);

CREATE INDEX idx_order_lines_order_id ON merchant.order_lines(order_id);
CREATE INDEX idx_order_lines_product_id ON merchant.order_lines(product_id);
```

---

### 6. `checkout_sessions` - 체크아웃 세션 (임시 장바구니)

```sql
CREATE TABLE merchant.checkout_sessions (
    -- Primary Key
    id                      VARCHAR(100) PRIMARY KEY,
    
    -- Buyer Information
    buyer_email             VARCHAR(255),
    buyer_name              VARCHAR(200),
    
    -- Session Status
    status                  VARCHAR(30) NOT NULL,  -- not_ready_for_payment, ready_for_payment, completed, canceled
    
    -- Pricing
    items_base_amount       BIGINT NOT NULL,
    items_discount_amount   BIGINT DEFAULT 0,
    subtotal_amount         BIGINT NOT NULL,
    fulfillment_amount      BIGINT DEFAULT 0,
    tax_amount              BIGINT DEFAULT 0,
    total_amount            BIGINT NOT NULL,
    currency                VARCHAR(3) NOT NULL DEFAULT 'KRW',
    
    -- Fulfillment Address
    fulfillment_name        VARCHAR(200),
    fulfillment_line_one    VARCHAR(500),
    fulfillment_line_two    VARCHAR(500),
    fulfillment_city        VARCHAR(100),
    fulfillment_state       VARCHAR(100),
    fulfillment_country     VARCHAR(2),
    fulfillment_postal_code VARCHAR(20),
    
    -- Selected Fulfillment Option
    selected_fulfillment_option VARCHAR(50),  -- standard, express, same_day
    
    -- Payment Provider
    payment_provider        VARCHAR(50) DEFAULT 'kakaopay',
    
    -- Order Reference (세션 완료 후 생성)
    order_id                VARCHAR(100),
    
    -- Timestamps
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at              TIMESTAMP NOT NULL,  -- 세션 만료 시각 (생성 후 30분)
    
    -- Constraints
    CONSTRAINT chk_session_status CHECK (status IN ('not_ready_for_payment', 'ready_for_payment', 'completed', 'canceled'))
);

CREATE INDEX idx_checkout_sessions_status ON merchant.checkout_sessions(status, created_at DESC);
CREATE INDEX idx_checkout_sessions_expires_at ON merchant.checkout_sessions(expires_at) WHERE status NOT IN ('completed', 'canceled');

CREATE TRIGGER trg_checkout_sessions_updated_at
    BEFORE UPDATE ON merchant.checkout_sessions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
```

---

### 7. `checkout_session_items` - 체크아웃 세션 상품 항목

```sql
CREATE TABLE merchant.checkout_session_items (
    id                      BIGSERIAL PRIMARY KEY,
    session_id              VARCHAR(100) NOT NULL REFERENCES merchant.checkout_sessions(id) ON DELETE CASCADE,
    
    -- Product Reference
    product_id              VARCHAR(100) NOT NULL,
    variant_id              VARCHAR(100),
    
    -- Quantity & Pricing
    quantity                INTEGER NOT NULL,
    unit_price_amount       BIGINT NOT NULL,
    discount_amount         BIGINT DEFAULT 0,
    subtotal_amount         BIGINT NOT NULL,
    tax_amount              BIGINT DEFAULT 0,
    total_amount            BIGINT NOT NULL,
    currency                VARCHAR(3) NOT NULL DEFAULT 'KRW',
    
    -- Metadata
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_session_item_quantity CHECK (quantity > 0),
    UNIQUE(session_id, product_id, variant_id)
);

CREATE INDEX idx_session_items_session_id ON merchant.checkout_session_items(session_id);
```

---

## 💳 PSP 도메인 (Schema: `psp`)

### 1. `payments` - 결제 트랜잭션

```sql
CREATE TABLE psp.payments (
    -- Primary Key
    id                      VARCHAR(100) PRIMARY KEY,
    
    -- Merchant Reference (Virtual FK)
    merchant_order_id       VARCHAR(100) NOT NULL UNIQUE,  -- 멱등성 키
    
    -- Payment Gateway (KakaoPay)
    pg_provider             VARCHAR(50) NOT NULL DEFAULT 'kakaopay',
    pg_tid                  VARCHAR(100),  -- 카카오페이 트랜잭션 ID (암호화 저장)
    pg_aid                  VARCHAR(100),  -- 카카오페이 승인 ID
    
    -- Payment Status
    status                  VARCHAR(20) NOT NULL,  -- READY, IN_PROGRESS, COMPLETED, FAILED, CANCELED
    
    -- Amount
    amount                  BIGINT NOT NULL,
    currency                VARCHAR(3) NOT NULL DEFAULT 'KRW',
    tax_free_amount         BIGINT DEFAULT 0,
    
    -- Buyer Information
    partner_user_id         VARCHAR(100) NOT NULL,
    
    -- Item Information (요약)
    item_name               VARCHAR(200) NOT NULL,  -- "나이키 에어맥스 270 외 2건"
    item_quantity           INTEGER NOT NULL,
    
    -- Redirect URLs (카카오페이 콜백)
    approval_url            VARCHAR(2048),
    cancel_url              VARCHAR(2048),
    fail_url                VARCHAR(2048),
    
    -- Payment Method (승인 후 저장)
    payment_method_type     VARCHAR(50),  -- CARD, MONEY (카카오머니)
    card_issuer             VARCHAR(100), -- 카드사 (마스킹)
    card_number_masked      VARCHAR(20),  -- 카드번호 마스킹 (예: ****-****-****-1234)
    
    -- Timestamps
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_at             TIMESTAMP,
    canceled_at             TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_payment_status CHECK (status IN ('READY', 'IN_PROGRESS', 'COMPLETED', 'FAILED', 'CANCELED')),
    CONSTRAINT chk_amount_positive CHECK (amount > 0)
);

-- Indexes
CREATE INDEX idx_payments_merchant_order_id ON psp.payments(merchant_order_id);
CREATE INDEX idx_payments_status ON psp.payments(status, created_at DESC);
CREATE INDEX idx_payments_pg_tid ON psp.payments(pg_tid);
CREATE INDEX idx_payments_created_at ON psp.payments(created_at DESC);

CREATE TRIGGER trg_payments_updated_at
    BEFORE UPDATE ON psp.payments
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
```

---

### 2. `payment_transactions` - 결제 이벤트 로그 (Audit Trail)

```sql
CREATE TABLE psp.payment_transactions (
    id                      BIGSERIAL PRIMARY KEY,
    payment_id              VARCHAR(100) NOT NULL REFERENCES psp.payments(id) ON DELETE CASCADE,
    
    -- Transaction Type
    transaction_type        VARCHAR(50) NOT NULL,  -- PREPARE, APPROVE, CANCEL, REFUND
    
    -- Status Transition
    previous_status         VARCHAR(20),
    new_status              VARCHAR(20) NOT NULL,
    
    -- Amount (부분 취소 대비)
    amount                  BIGINT,
    
    -- External Reference
    pg_response             JSONB,  -- 카카오페이 응답 원본 (디버깅용)
    
    -- Error Info
    error_code              VARCHAR(50),
    error_message           TEXT,
    
    -- Metadata
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              VARCHAR(100),  -- 시스템 또는 관리자 ID
    
    -- Constraints
    CONSTRAINT chk_transaction_type CHECK (transaction_type IN ('PREPARE', 'APPROVE', 'CANCEL', 'REFUND'))
);

CREATE INDEX idx_transactions_payment_id ON psp.payment_transactions(payment_id, created_at DESC);
CREATE INDEX idx_transactions_type ON psp.payment_transactions(transaction_type, created_at DESC);
```

---

### 3. `payment_partner_meta` - 결제 제공자 설정

```sql
CREATE TABLE psp.payment_partner_meta (
    id                      SERIAL PRIMARY KEY,
    
    -- Provider Info
    provider                VARCHAR(50) NOT NULL UNIQUE,  -- kakaopay, stripe, toss
    
    -- Credentials (암호화 저장)
    client_id               VARCHAR(255),
    client_secret           VARCHAR(255),  -- 암호화
    secret_key              VARCHAR(255),  -- 암호화
    merchant_cid            VARCHAR(100),  -- 가맹점 코드
    
    -- Configuration
    api_base_url            VARCHAR(255) NOT NULL,
    is_production           BOOLEAN DEFAULT FALSE,
    
    -- Metadata
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 초기 데이터 (카카오페이)
INSERT INTO psp.payment_partner_meta (provider, merchant_cid, api_base_url, is_production)
VALUES ('kakaopay', 'TC0ONETIME', 'https://open-api.kakaopay.com', FALSE);
```

---

### 4. `idempotency_keys` - 멱등성 키 관리

```sql
CREATE TABLE psp.idempotency_keys (
    key                     VARCHAR(255) PRIMARY KEY,
    
    -- Request Info
    request_method          VARCHAR(10) NOT NULL,  -- POST, PUT
    request_path            VARCHAR(500) NOT NULL,
    request_body_hash       VARCHAR(64),  -- SHA-256 해시
    
    -- Response Cache
    response_status_code    INTEGER,
    response_body           JSONB,
    
    -- Metadata
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at              TIMESTAMP NOT NULL  -- 24시간 후 만료
);

CREATE INDEX idx_idempotency_expires_at ON psp.idempotency_keys(expires_at);

-- 만료된 키 자동 삭제 (Cron Job 또는 Scheduler)
-- DELETE FROM psp.idempotency_keys WHERE expires_at < CURRENT_TIMESTAMP;
```

---

## 🔐 보안 및 암호화

### 암호화 대상 필드

| 테이블 | 필드 | 암호화 알고리즘 | 이유 |
|--------|------|-----------------|------|
| `psp.payments` | `pg_tid` | AES-256-GCM | 민감한 트랜잭션 ID |
| `psp.payment_partner_meta` | `client_secret` | AES-256-GCM | API 시크릿 |
| `psp.payment_partner_meta` | `secret_key` | AES-256-GCM | 결제 시크릿 키 |

### 암호화 구현 예시 (Kotlin)

```kotlin
// EncryptionService.kt
interface EncryptionService {
    fun encrypt(plainText: String): String
    fun decrypt(cipherText: String): String
}

// AesGcmEncryptionService.kt
class AesGcmEncryptionService(
    private val secretKey: SecretKey  // AWS KMS 또는 Vault에서 로드
) : EncryptionService {
    override fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = ByteArray(12).apply { SecureRandom().nextBytes(this) }
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        val cipherText = cipher.doFinal(plainText.toByteArray())
        return Base64.getEncoder().encodeToString(iv + cipherText)
    }
    
    override fun decrypt(cipherText: String): String {
        val decoded = Base64.getDecoder().decode(cipherText)
        val iv = decoded.sliceArray(0..11)
        val encrypted = decoded.sliceArray(12 until decoded.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        return String(cipher.doFinal(encrypted))
    }
}
```

---

## 📊 데이터 마이그레이션 (Flyway)

### 마이그레이션 파일 구조

```
acp-merchant/src/main/resources/db/migration/
├── V1__create_products.sql
├── V2__create_product_variants.sql
├── V3__create_product_images.sql
├── V4__create_orders.sql
├── V5__create_order_lines.sql
├── V6__create_checkout_sessions.sql
└── V7__create_checkout_session_items.sql

acp-psp/src/main/resources/db/migration/
├── V1__create_payments.sql
├── V2__create_payment_transactions.sql
├── V3__create_payment_partner_meta.sql
└── V4__create_idempotency_keys.sql
```

---

## 🔍 쿼리 최적화 전략

### 1. 인덱스 전략

- **복합 인덱스**: 자주 함께 조회되는 컬럼 (예: `status`, `created_at`)
- **부분 인덱스**: 조건부 인덱스로 크기 최소화 (예: `WHERE deleted_at IS NULL`)
- **Full-Text Search**: 상품 검색 최적화 (`GIN` 인덱스)

### 2. 파티셔닝 (Future)

대용량 데이터 처리를 위한 테이블 파티셔닝:

```sql
-- 주문 테이블을 월별로 파티셔닝
CREATE TABLE merchant.orders_2025_01 PARTITION OF merchant.orders
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
```

### 3. 읽기 복제본 (Read Replica)

- **Master**: 쓰기 전용
- **Replica**: 읽기 전용 (상품 피드, 주문 조회)

---

## 📈 성능 메트릭

### 목표 성능 지표

| 메트릭 | 목표 | 측정 방법 |
|--------|------|-----------|
| 상품 피드 조회 | P95 < 100ms | Prometheus `http_server_requests_seconds` |
| 주문 생성 | P95 < 500ms | 동일 |
| 결제 준비 | P95 < 1s | 동일 |
| DB 커넥션 풀 사용률 | < 80% | `hikaricp_connections_active` |
| 쿼리 실행 시간 | P95 < 50ms | `pg_stat_statements` |

---

## 🧪 테스트 데이터

### Mock 데이터 생성 스크립트

```sql
-- 상품 100개 생성
INSERT INTO merchant.products (id, title, description, link, price_amount, currency, availability, stock_quantity, brand, product_category)
SELECT
    'prod_' || generate_series,
    '테스트 상품 ' || generate_series,
    '이것은 테스트 상품입니다.',
    'https://merchant.example.com/products/prod_' || generate_series,
    (random() * 100000 + 10000)::BIGINT,
    'KRW',
    (ARRAY['in_stock', 'out_of_stock'])[floor(random() * 2 + 1)],
    floor(random() * 100)::INTEGER,
    (ARRAY['Nike', 'Adidas', 'Apple', 'Samsung'])[floor(random() * 4 + 1)],
    (ARRAY['신발', '전자제품', '의류', '도서'])[floor(random() * 4 + 1)]
FROM generate_series(1, 100);
```

---

## 📚 참고 자료

- [PostgreSQL 16 Documentation](https://www.postgresql.org/docs/16/)
- [jOOQ Best Practices](https://www.jooq.org/doc/latest/manual/sql-building/best-practices/)
- [Flyway Migration Guide](https://flywaydb.org/documentation/)
- [Database Indexing Strategies](https://use-the-index-luke.com/)
- [OpenAI Product Feed Spec](https://developers.openai.com/commerce/specs/feed)