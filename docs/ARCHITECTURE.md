# 플랫폼 아키텍처 문서

## 📋 목차

1. [시스템 개요](#시스템-개요)
2. [아키텍처 구조](#아키텍처-구조)
3. [서비스 통신 방식](#서비스-통신-방식)
4. [인증 및 권한 관리](#인증-및-권한-관리)
5. [데이터베이스 구조](#데이터베이스-구조)
6. [구현 단계](#구현-단계)

---

## 🎯 시스템 개요

이 플랫폼은 **Portal을 중심으로 여러 프로젝트를 연결하고 관리하는 통합 플랫폼**입니다.

### 핵심 특징
- ✅ **중앙 집중식 인증**: Portal에서 가입한 계정으로 모든 프로젝트 접근
- ✅ **독립적인 권한 구조**: 각 프로젝트마다 독립적인 권한 관리
- ✅ **다양한 데이터베이스**: 프로젝트별로 다른 DB 사용 (MariaDB, Redis, PostgreSQL 등)
- ✅ **마이크로서비스 아키텍처**: 각 서비스 독립 배포 및 확장

---

## 🏗️ 아키텍처 구조

### 전체 구조도

```
┌─────────────────────────────────────────────────────────┐
│                    Portal Frontend                      │
│              http://localhost:3000                      │
│  - 대시보드                                              │
│  - 관리자 페이지                                         │
│  - 프로젝트 목록                                         │
└──────────────────┬──────────────────────────────────────┘
                   │
                   │ 모든 요청
                   ▼
┌─────────────────────────────────────────────────────────┐
│              API Gateway (Phase 2)                       │
│              http://localhost:8000                      │
│  - 라우팅                                                │
│  - JWT 검증                                              │
│  - 로드 밸런싱                                           │
└──────┬──────────────────────────────┬───────────────────┘
       │                              │
       ▼                              ▼
┌──────────────┐              ┌──────────────┐
│ Auth 서비스  │              │ Chat 서비스  │
│ :8080        │              │ :8081        │
│ MariaDB      │              │ Redis        │
│              │              │              │
│ - 인증/인가  │              │ - 채팅 기능  │
│ - 사용자 관리│              │ - 실시간 통신│
└──────────────┘              └──────────────┘
       │                              │
       ▼                              ▼
┌──────────────┐              ┌──────────────┐
│ Shop 서비스  │              │ ...          │
│ :8082        │              │              │
│ PostgreSQL   │              │              │
│              │              │              │
│ - 쇼핑 기능  │              │              │
└──────────────┘              └──────────────┘
```

### 현재 구조 (Phase 1)

```
Portal Frontend
  ├─ /api/auth/* → Auth 서비스 (8080)
  ├─ /api/chat/* → Chat 서비스 (8081)  [예정]
  └─ /api/shop/* → Shop 서비스 (8082)  [예정]
```

---

## 🔄 서비스 통신 방식

### Phase 1: 직접 통신 (현재)

**프론트엔드 → 서비스 직접 호출**

```javascript
// 프론트엔드에서 각 서비스 직접 호출
const API_BASE_URLS = {
  auth: 'http://localhost:8080/api',
  chat: 'http://localhost:8081/api',
  shop: 'http://localhost:8082/api',
};
```

**장점:**
- 구현 간단
- Gateway 오버헤드 없음
- 빠른 시작 가능

**단점:**
- 서비스가 늘어나면 관리 복잡
- CORS 설정 각 서비스마다 필요
- 인증 검증 각 서비스마다 구현 필요

---

### Phase 2: API Gateway 패턴 (권장)

**프론트엔드 → API Gateway → 각 서비스**

```
Portal Frontend → API Gateway (8000) → Auth (8080), Chat (8081), Shop (8082)
```

**구현 방법: Spring Cloud Gateway**

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: http://localhost:8080
          predicates:
            - Path=/api/auth/**
          filters:
            - StripPrefix=1
        
        - id: chat-service
          uri: http://localhost:8081
          predicates:
            - Path=/api/chat/**
          filters:
            - StripPrefix=1
```

**장점:**
- 단일 진입점: 프론트엔드는 하나의 엔드포인트만 사용
- 인증 중앙화: Gateway에서 JWT 검증
- 서비스 독립성: 각 서비스는 독립적으로 배포/확장
- 로드 밸런싱: 여러 인스턴스 관리 용이

---

### 서비스 간 통신 (서비스 → 서비스)

#### 1. 동기 통신: RestTemplate / WebClient

```java
// Chat 서비스에서 Auth 서비스 호출
@Service
public class ChatService {
    private final WebClient authWebClient;
    
    public boolean hasPermission(String username, String project) {
        return authWebClient.get()
            .uri("/api/auth/user/permissions?username={username}&project={project}", 
                 username, project)
            .retrieve()
            .bodyToMono(Boolean.class)
            .block();
    }
}
```

#### 2. 비동기 통신: Message Queue (선택)

서비스 간 이벤트 기반 통신이 필요하면:
- RabbitMQ
- Kafka
- Redis Pub/Sub

---

## 🔐 인증 및 권한 관리

### SSO (Single Sign-On) 흐름

```
1. Portal에서 로그인
   ↓
2. Auth 서비스에서 JWT 토큰 발급
   ↓
3. Portal에 토큰 저장 (HttpOnly Cookie)
   ↓
4. 다른 서비스 접근 시:
   - Portal에서 토큰 전달
   - 또는 Auth 서비스에서 토큰 검증
   ↓
5. 각 서비스에서 권한 확인
   - JWT에 권한 정보 포함
   - 또는 Auth 서비스에 권한 조회 API 호출
```

### JWT 토큰 구조

**현재:**
```json
{
  "sub": "username",
  "iat": 1234567890,
  "exp": 1234571490
}
```

**확장 (권한 포함):**
```json
{
  "sub": "username",
  "roles": ["PORTAL_ADMIN", "CHAT_USER"],
  "iat": 1234567890,
  "exp": 1234571490
}
```

### 권한 확인 방식

#### 옵션 A: JWT에 권한 포함 (권장)

**장점:**
- 각 서비스에서 DB 조회 없이 권한 확인 가능
- 빠른 응답

**단점:**
- 권한 변경 시 토큰 재발급 필요
- 토큰 크기 증가

#### 옵션 B: 권한 조회 API 제공

```java
// Auth 서비스에 권한 조회 API
GET /api/auth/user/permissions?username=jh&project=CHAT
```

**장점:**
- 실시간 권한 반영
- 토큰 크기 작음

**단점:**
- 매번 API 호출 필요
- 성능 저하 가능

---

## 🗄️ 데이터베이스 구조

### 각 프로젝트별 독립적인 DB

| 서비스 | 데이터베이스 | 용도 |
|--------|-------------|------|
| Auth | MariaDB | 사용자 정보, 권한, 토큰 |
| Chat | Redis | 실시간 채팅 데이터, 세션 |
| Shop | PostgreSQL | 상품 정보, 주문 데이터 |
| Portal | MariaDB (예정) | 프로젝트 목록, 설정 |

### Auth 서비스 DB 구조

```sql
TB_USER              -- 사용자 기본 정보
TB_ROLE              -- 권한 정보 (프로젝트별 권한)
TB_USER_ROLE         -- 사용자-권한 매핑
TB_LOGIN_HISTORY     -- 로그인 이력
TB_USER_REFRESH_TOKEN -- 리프레시 토큰
```

### 권한 구조 예시

```
PORTAL_ADMIN     → Portal 관리자
PORTAL_USER      → Portal 일반 사용자
CHAT_ADMIN       → Chat 관리자
CHAT_USER        → Chat 일반 사용자
SHOP_ADMIN       → Shop 관리자
SHOP_USER        → Shop 일반 사용자
```

**각 프로젝트마다 독립적인 권한 구조**

---

## 📈 구현 단계

### Phase 1: 현재 구조 유지 + 서비스 추가 ✅

**목표:**
- Portal 대시보드 구현
- 관리자 페이지 구현
- 프로젝트 목록 표시

**작업:**
1. Portal 대시보드 컴포넌트 생성
2. 프로젝트 목록 API (Mock 데이터)
3. 관리자 페이지 라우팅

---

### Phase 2: API Gateway 도입

**목표:**
- Spring Cloud Gateway 프로젝트 생성
- 라우팅 설정
- JWT 검증 필터 추가

**작업:**
1. Gateway 프로젝트 생성
2. 라우팅 설정 (Auth, Chat, Shop)
3. JWT 검증 필터 구현

---

### Phase 3: 다른 서비스 추가

**목표:**
- Chat 서비스 추가 (Redis)
- Shop 서비스 추가 (PostgreSQL)
- 각 서비스에서 권한 확인

**작업:**
1. Chat 서비스 프로젝트 생성
2. Shop 서비스 프로젝트 생성
3. 권한 조회 API (Auth 서비스)
4. 각 서비스에서 권한 확인 로직

---

### Phase 4: 고급 기능

**목표:**
- 권한 관리 UI (Portal)
- 프로젝트별 권한 할당
- 권한 이력 관리

---

## 🔧 기술 스택

### 프론트엔드
- **React**: UI 라이브러리
- **TanStack Query**: 서버 상태 관리
- **Zustand**: 클라이언트 상태 관리
- **Axios**: HTTP 클라이언트
- **React Router**: 라우팅

### 백엔드
- **Spring Boot**: Java 프레임워크
- **Spring Cloud Gateway**: API Gateway (Phase 2)
- **MyBatis**: ORM 매퍼
- **Spring Security**: 보안 프레임워크
- **JWT (JJWT)**: 토큰 생성/검증

### 데이터베이스
- **MariaDB**: Auth 서비스, Portal
- **Redis**: Chat 서비스
- **PostgreSQL**: Shop 서비스

---

## 📝 참고사항

### 서비스 통신 방식 선택 가이드

**현재 (Phase 1):**
- 서비스가 2-3개: 직접 통신
- 빠른 프로토타이핑 필요
- 간단한 구조 선호

**향후 (Phase 2):**
- 서비스가 4개 이상: API Gateway
- 중앙 집중식 인증 필요
- 확장성 중요

### 권한 관리 전략

**현재:**
- JWT에 username만 포함
- 각 서비스에서 권한 조회 API 호출

**향후:**
- JWT에 권한 정보 포함 (성능 최적화)
- 권한 변경 시 토큰 재발급

---

## 🚀 다음 단계

1. ✅ Portal 대시보드 구현
2. ✅ 프로젝트 목록 표시
3. ✅ 관리자 페이지 구현
4. ⏳ API Gateway 도입 (서비스 3개 이상일 때)
5. ⏳ Chat 서비스 추가
6. ⏳ Shop 서비스 추가

