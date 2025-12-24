# 🗺️ Project Roadmap & Todo List

> **Vision**: Build a production-grade Agentic Commerce ecosystem where an AI Agent authenticates, discovers products (sourced from real-world platforms like Cafe24), and executes payments via a secure PSP (KakaoPay).

---

## ✅ Phase 0: Foundation (Architecture & Skeleton)
- [x] **Multi-Module Structure**: Separate `merchant`, `psp`, `shared`, `client`.
- [x] **Hexagonal Architecture**: Isolate Domain, Ports, and Adapters.
- [x] **Infrastructure**: Docker Compose for PostgreSQL 16.
- [x] **Tech Stack Enforced**: JDK 21, Kotlin 2.1, Spring Boot 3.5.3, jOOQ, Virtual Threads.
- [x] **Documentation**: Architecture Overview, Sequence Diagrams, DB Schema.

---

## 🏗️ Phase 1: The PSP Server (KakaoPay Wrapper)
**Goal**: A robust payment gateway that wraps KakaoPay and provides standard APIs for the Merchant.

### 1.1 Core Logic
- [ ] **State Machine**: Implement `PaymentStatus` flow (`READY` -> `IN_PROGRESS` -> `DONE` / `FAILED`).
- [ ] **Idempotency**: Ensure `POST /prepare` is safe to retry using `merchant_order_id`.

### 1.2 KakaoPay Integration
- [ ] **Ready API**: Call KakaoPay `/v1/payment/ready` and return `next_redirect_pc_url`.
- [ ] **Approve API**: Handle the redirect callback, exchange `pg_token` for final approval.
- [ ] **Error Handling**: Gracefully handle user cancellation, timeouts, and API failures.

### 1.3 Testing
- [ ] **Mock Server**: Create a mock KakaoPay server for integration tests (don't rely only on real API).

---

## 🛍️ Phase 2: The Merchant Server (Cafe24 Simulation)
**Goal**: A "Real" shop that provides product feeds and handles order lifecycles.

### 2.1 Product Sourcing
- [ ] **Product Feed API**: Implement `GET /feed` compliant with OpenAI ACP Spec.
- [ ] **Source Strategy**:
    - [ ] Option A: Mock Data (Initial).
    - [ ] Option B: Cafe24 Open API Proxy (Fetch real products from a demo store).

### 2.2 Checkout Flow
- [ ] **Session Creation**: Implement `POST /checkout_sessions` (ACP Spec).
- [ ] **PSP Integration**: Call `acp-psp` via `WebClient` to get payment URLs.
- [ ] **Order State**: Manage `PENDING` -> `AUTHORIZED` -> `COMPLETED`.

---

## 🤖 Phase 3: The Agent & Client Experience
**Goal**: Visualize the invisible "Agent Protocol" through a beautiful UI.

### 3.1 Agent Simulator (Web/Desktop)
- [ ] **Chat Interface**: A UI that looks like ChatGPT.
- [ ] **Protocol Debugger**: View the raw JSON payloads (Feed, Checkout) exchanged during the chat.
- [ ] **"Buy for me" Action**: Trigger the full ACP flow with a single click.

### 3.2 Real Agent Integration (The "Last Mile")
- [ ] **Public Exposure**: Use `ngrok` or Cloudflare Tunnel to expose local `8080`.
- [ ] **OpenAI Actions**: Configure a custom GPT to actually talk to our Merchant Server.

---

## 📊 Phase 4: Observability & SRE (High Quality)
**Goal**: Make the system transparent and monitorable.

### 4.1 Metrics (Micrometer + Prometheus)
- [ ] **Business Metrics**: "Orders per minute", "Payment Success Rate", "Revenue".
- [ ] **System Metrics**: Virtual Thread pinning count, JDBC connection pool usage.

### 4.2 Tracing (OpenTelemetry)
- [ ] **Distributed Tracing**: Trace request ID from Agent -> Merchant -> PSP -> KakaoPay.
- [ ] **Logging**: Structured JSON logging (Logback).

---

## 🧪 Phase 5: Technical Debt & Refactoring
- [ ] **jOOQ CodeGen**: Automate generation via Testcontainers in CI pipeline.
- [ ] **Security**: Encrypt sensitive fields (tokens) in DB at rest.
- [ ] **CI/CD**: GitHub Actions workflow for build and test.
