# 🗄️ Database Schema

We use **PostgreSQL** with **jOOQ**.

## 🛒 Merchant Domain

### `products`
Stores product information compliant with OpenAI Product Feed spec.
*   `id` (PK)
*   `price_amount`, `currency`
*   `availability` (in_stock, etc.)

### `orders`
Represents the user's order intent.
*   `id` (PK, UUID)
*   `user_id`
*   `status` (CREATED, AUTHORIZED, COMPLETED, CANCELED)
*   `total_amount`
*   `payment_request_id` (FK to `payments.id`)

### `order_lines`
Items within an order.
*   `unit_price`, `total_price` (Snapshot of price at purchase time)
*   `currency`

---

## 💳 Payment Domain

### `payments`
Tracks the lifecycle of a payment transaction.
*   `id` (PK)
*   `pg_tid` (External Transaction ID from KakaoPay)
*   `status` (READY, IN_PROGRESS, DONE, FAILED)
*   `amount`, `currency`

### `payment_partner_meta`
Configuration for Payment Providers (KakaoPay, Stripe, etc.).
*   `provider` (KAKAOPAY)
*   `client_id`, `merchant_cid`
