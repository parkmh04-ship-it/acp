# 🛒 Agentic Commerce Protocol (ACP) Reference Implementation

> **High-Performance, Type-Safe Commerce System for the AI Agent Era.**

본 프로젝트는 OpenAI의 **Agentic Commerce Protocols (ACP)**을 준수하는 레퍼런스 커머스 시스템입니다.
AI 에이전트가 쇼핑몰(Merchant)과 대화하고 결제 중계자(PSP)를 통해 안전하게 결제를 완료하는 **2-Server 아키텍처**를 시뮬레이션합니다.

## 📚 Documentation
*   [Roadmap & Todo List](docs/TODO.md) - 프로젝트 진행 상황 및 계획
*   [Architecture Overview](docs/ARCHITECTURE.md) - 상세 시퀀스 다이어그램 및 설계 철학
*   [Database Schema](docs/DB_SCHEMA.md) - Merchant/PSP 분리 스키마 정의

## 🏗️ 2-Server Architecture

| 서버 | 포트 | 역할 |
| :--- | :--- | :--- |
| **Merchant Server** | `8080` | 상품 피드 제공, 주문 생성 및 관리 |
| **PSP Server** | `8081` | 결제 중계 (KakaoPay 연동), 결제 트랜잭션 관리 |

---

## 🛠 Tech Stack

*   **Language**: Kotlin (JVM 21)
*   **Framework**: Spring Boot 3.5.3 (Virtual Threads Enabled)
*   **Database**: PostgreSQL 16 + jOOQ (Type-Safe SQL)
*   **Client**: Kotlin Multiplatform (Compose for Desktop)

---

## 🚀 Getting Started

### 1. 인프라 실행 (DB)
```bash
docker-compose -f docker/docker-compose.yml up -d
```

### 2. 환경 변수 설정
`.env` 파일에 카카오페이 API 키를 설정합니다.
```bash
cp .env.template .env
# KAKAOPAY_SECRET_KEY_DEV 입력
```

### 3. 서버 실행
각 서버는 별도의 프로세스로 실행합니다.
```bash
# Merchant Server
./gradlew :acp-merchant:bootRun

# PSP Server
./gradlew :acp-psp:bootRun
```

---

## 📜 Design Philosophy
*   **Explicit over Implicit**: JPA 대신 jOOQ를 사용하여 명시적인 데이터 제어.
*   **Virtual Threads First**: 복잡한 비동기 코드 없이 높은 동시성 확보.
*   **Physical Separation**: Merchant와 PSP를 물리적으로 분리하여 실제 네트워크 환경 시뮬레이션.
