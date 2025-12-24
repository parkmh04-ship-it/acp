# 🏗️ Architecture: Dual-Server System (Merchant & PSP)

To fully simulate the Agentic Commerce ecosystem, we separate the system into two distinct servers.

## 🗺️ System Overview

| Component | Port | Role | Database Schema |
| :--- | :--- | :--- | :--- |
| **Merchant Server** | `8080` | Product Feed, Order Management | `merchant_db` (orders, products) |
| **PSP Server** | `8081` | Payment Gateway Wrapper (KakaoPay) | `psp_db` (payments, transactions) |
| **Agent Simulator** | CLI | Simulates OpenAI Agent | N/A |

---

## 🔄 The Payment Flow (Sequence Diagram)

This diagram illustrates how an Agent buys a product using the "Delegated Payment" flow via KakaoPay.

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Agent as AI Agent
    participant Merchant as Merchant Server (:8080)
    participant PSP as PSP Server (:8081)
    participant KPay as KakaoPay (External)

    Note over Agent, Merchant: 1. Checkout Phase
    Agent->>Merchant: POST /checkout_sessions (Items, Buyer)
    Merchant->>Merchant: Create Order (PENDING)
    
    Note over Merchant, PSP: 2. Payment Preparation (S2S)
    Merchant->>PSP: POST /api/v1/payments/prepare (Amount, OrderID)
    PSP->>KPay: POST /v1/payment/ready
    KPay-->>PSP: tid, next_redirect_pc_url
    PSP->>PSP: Save Transaction (READY)
    PSP-->>Merchant: payment_id, redirect_url
    
    Note over Merchant, Agent: 3. Handover to Agent
    Merchant-->>Agent: CheckoutSessionResponse (Status: READY, next_action_url)
    
    Note over Agent, User: 4. User Authorization
    Agent->>User: "Please pay here: [URL]"
    User->>KPay: Open URL & Approve Payment
    
    Note over KPay, PSP: 5. Finalization (Redirect)
    KPay->>PSP: GET /api/v1/payments/callback/success?pg_token=...
    PSP->>KPay: POST /v1/payment/approve (Finalize)
    KPay-->>PSP: Success
    PSP->>PSP: Update Transaction (COMPLETED)
    
    Note over PSP, Merchant: 6. Settlement / Sync
    PSP->>Merchant: (Optional) Webhook: Payment Success
    Merchant->>PSP: GET /api/v1/payments/{id} (Verify Status)
    Merchant->>Merchant: Update Order (COMPLETED)
```

---

## 🏛️ Module Structure

```text
acp/
├── acp-merchant/       # [Server] Merchant Logic (Spring Boot)
│   └── src/main/resources/db/migration # Merchant DB Schema
├── acp-psp/            # [Server] PSP Logic (Spring Boot)
│   └── src/main/resources/db/migration # PSP DB Schema
├── acp-shared/         # [Common] DTOs shared by everyone
└── acp-client/         # [Client] Agent Simulator
```

## 🔌 API Contracts

### Merchant Server (:8080)
*   `GET /feed`: Product Feed (OpenAI Spec)
*   `POST /checkout_sessions`: Create Order

### PSP Server (:8081)
*   `POST /api/v1/payments/prepare`: Request Payment URL
*   `GET /api/v1/payments/{id}`: Check Payment Status
*   `GET /api/v1/payments/callback/success`: Handle KakaoPay Redirect
