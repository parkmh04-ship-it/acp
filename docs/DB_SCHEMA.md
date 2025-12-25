# 🗄️ 데이터베이스 스키마

**PostgreSQL**을 사용하며 **jOOQ**로 접근합니다.
시스템은 `merchant`와 `psp` 두 개의 스키마로 분리되어 있습니다.

## 🛒 Merchant 도메인 (Schema: `merchant`)

### `products`
OpenAI 상품 피드 사양을 준수하는 상품 정보를 저장합니다.
*   `id` (PK): SKU 또는 UUID
*   `price_amount`, `currency`: 가격 및 통화
*   `availability`: 재고 상태 (in_stock 등)

### `orders`
사용자의 주문 의도를 나타냅니다.
*   `id` (PK, UUID)
*   `user_id`: 사용자 식별자
*   `status`: 상태 (CREATED, AUTHORIZED, COMPLETED, CANCELED)
*   `total_amount`: 총 주문 금액
*   `payment_request_id`: PSP의 결제 ID 참조 (Virtual FK)

### `order_lines`
주문에 포함된 개별 상품 항목입니다.
*   `unit_price`, `total_price`: 구매 시점의 가격 스냅샷
*   `currency`: 통화

---

## 💳 Payment 도메인 (Schema: `psp`)

### `payments`
결제 트랜잭션의 생명주기를 추적합니다.
*   `id` (PK): PSP 내부 ID
*   `pg_tid`: 카카오페이 등 외부 트랜잭션 ID
*   `status`: 상태 (READY, IN_PROGRESS, DONE, FAILED)
*   `amount`, `currency`: 결제 금액 및 통화

### `payment_partner_meta`
결제 제공자(카카오페이, 스트라이프 등)를 위한 설정 정보입니다.
*   `provider`: 제공자 타입 (KAKAOPAY)
*   `client_id`, `merchant_cid`: 연동 키 및 가맹점 ID