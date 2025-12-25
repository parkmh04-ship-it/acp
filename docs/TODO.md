# 🗺️ 프로젝트 로드맵 및 할 일 목록

> **비전**: AI 에이전트가 인증하고, 상품을 발견하고(Cafe24 등 실제 플랫폼 소싱), 안전한 PSP(카카오페이)를 통해 결제를 실행하는 상용 수준의 에이전트 커머스 생태계를 구축합니다.

---

## ✅ 0단계: 기반 구축 (아키텍처 및 뼈대)
- [x] **멀티 모듈 구조**: `merchant`, `psp`, `shared`, `client` 분리.
- [x] **헥사고날 아키텍처**: 도메인, 포트, 어댑터 격리.
- [x] **인프라**: PostgreSQL 16용 Docker Compose.
- [x] **기술 스택 확정**: JDK 21, Kotlin 2.1, Spring Boot 3.5.3, jOOQ, Virtual Threads.
- [x] **문서화**: 아키텍처 개요, 시퀀스 다이어그램, DB 스키마.

---

## 🏗️ 1단계: PSP 서버 (카카오페이 래퍼)
**목표**: 카카오페이를 감싸고 Merchant에게 표준 API를 제공하는 견고한 결제 게이트웨이 구축.

### 1.1 핵심 로직
- [x] **상태 머신**: `PaymentStatus` 흐름 구현 (`READY` -> `IN_PROGRESS` -> `DONE` / `FAILED`).
- [x] **멱등성**: `merchant_order_id`를 사용하여 `POST /prepare` 재시도 안전성 보장.

### 1.2 카카오페이 연동
- [ ] **준비(Ready) API**: 카카오페이 `/v1/payment/ready` 호출 및 `next_redirect_pc_url` 반환.
- [ ] **승인(Approve) API**: 리다이렉트 콜백 처리, `pg_token`을 최종 승인으로 교환.
- [ ] **에러 처리**: 사용자 취소, 타임아웃, API 실패를 우아하게 처리.

### 1.3 테스트
- [x] **통합 테스트**: 실제 API 대신 모의(Mock) 동작을 검증하는 통합 테스트 작성.

---

## 🛍️ 2단계: Merchant 서버 (쇼핑몰 시뮬레이션)
**목표**: 상품 피드를 제공하고 주문 생명주기를 관리하는 "실제" 상점 구현.

### 2.1 상품 소싱
- [x] **상품 피드 API**: OpenAI ACP 사양을 준수하는 `GET /feed` 구현.
- [ ] **소싱 전략**:
    - [x] 옵션 A: 모의 데이터(Mock Data) (초기).
    - [ ] 옵션 B: Cafe24 Open API 프록시 (데모 스토어에서 실제 상품 가져오기).

### 2.2 체크아웃 흐름
- [ ] **세션 생성**: `POST /checkout_sessions` 구현 (ACP 사양).
- [ ] **PSP 연동**: `WebClient`를 통해 `acp-psp`를 호출하여 결제 URL 획득.
- [ ] **주문 상태**: `PENDING` -> `AUTHORIZED` -> `COMPLETED` 관리.

---

## 🤖 3단계: 에이전트 및 클라이언트 경험
**목표**: 아름다운 UI를 통해 보이지 않는 "에이전트 프로토콜" 시각화.

### 3.1 에이전트 시뮬레이터 (웹/데스크톱)
- [ ] **채팅 인터페이스**: ChatGPT와 유사한 UI.
- [ ] **프로토콜 디버거**: 채팅 중 교환되는 원시 JSON 페이로드(Feed, Checkout) 보기.
- [ ] **"나를 위해 구매해줘" 액션**: 클릭 한 번으로 전체 ACP 흐름 트리거.

### 3.2 실제 에이전트 연동 ("라스트 마일")
- [ ] **외부 노출**: `ngrok` 또는 Cloudflare Tunnel을 사용하여 로컬 `8080` 포트 노출.
- [ ] **OpenAI Actions**: 커스텀 GPT를 구성하여 실제로 Merchant 서버와 대화하도록 설정.

---

## 📊 4단계: 관측 가능성 및 SRE (고품질)
**목표**: 시스템을 투명하고 모니터링 가능하게 만들기.

### 4.1 메트릭 (Micrometer + Prometheus)
- [ ] **비즈니스 메트릭**: "분당 주문 수", "결제 성공률", "매출".
- [ ] **시스템 메트릭**: Virtual Thread 피닝(pinning) 수, JDBC 커넥션 풀 사용량.

### 4.2 트레이싱 (OpenTelemetry)
- [ ] **분산 트레이싱**: Agent -> Merchant -> PSP -> KakaoPay로 이어지는 요청 ID 추적.
- [ ] **로깅**: 구조화된 JSON 로깅 (Logback).

---

## 🧪 5단계: 기술 부채 및 리팩토링
- [x] **jOOQ CodeGen**: Gradle 태스크 및 빌드 프로세스 최적화.
- [ ] **보안**: DB 저장 시 민감한 필드(토큰) 암호화.
- [ ] **CI/CD**: 빌드 및 테스트를 위한 GitHub Actions 워크플로우.