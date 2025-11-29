# JH Platform 마스터 가이드

> **통합 플랫폼 프로젝트 - Portal을 중심으로 여러 프로젝트를 연결하고 관리하는 시스템**
> 
> 이 문서는 프로젝트의 모든 내용을 종합한 메인 가이드입니다. 지속적으로 업데이트됩니다.

**최종 업데이트**: 2024-11-29

---

## 📋 목차

1. [프로젝트 개요](#프로젝트-개요)
2. [시스템 아키텍처](#시스템-아키텍처)
3. [기술 스택](#기술-스택)
4. [프로젝트 구조](#프로젝트-구조)
5. [배포 가이드](#배포-가이드)
6. [새 프로젝트 추가](#새-프로젝트-추가)
7. [도메인 설정](#도메인-설정)
8. [외부 접속 설정](#외부-접속-설정)
9. [문제 해결](#문제-해결)
10. [FAQ](#faq)

---

## 🎯 프로젝트 개요

### 핵심 개념

JH Platform은 **Portal을 중심으로 여러 프로젝트를 연결하고 관리하는 통합 플랫폼**입니다.

### 주요 특징

- ✅ **중앙 집중식 인증**: Portal에서 가입한 계정으로 모든 프로젝트 접근
- ✅ **독립적인 권한 구조**: 각 프로젝트마다 독립적인 권한 관리
- ✅ **다양한 데이터베이스**: 프로젝트별로 다른 DB 사용 (MariaDB, Redis, PostgreSQL 등)
- ✅ **마이크로서비스 아키텍처**: 각 서비스 독립 배포 및 확장
- ✅ **Docker 기반 배포**: 컨테이너화된 배포로 일관된 환경 보장

### 사용 시나리오

```
1. 사용자가 Portal에 가입
2. Portal에서 여러 프로젝트 목록 확인
3. 원하는 프로젝트에 가입/접근
4. 각 프로젝트는 독립적으로 동작하지만 Portal 계정으로 통합 관리
```

---

## 🏗️ 시스템 아키텍처

### 전체 구조도

```
┌─────────────────────────────────────────────────────────┐
│                    사용자 브라우저                      │
│         http://외부IP:8181 또는 https://도메인         │
└──────────────────┬──────────────────────────────────────┘
                   │
                   │ HTTP/HTTPS 요청
                   ▼
┌─────────────────────────────────────────────────────────┐
│         Windows 서버 (Docker 환경)                      │
│                                                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │  portal-frontend (Nginx)                         │  │
│  │  - 포트: 80 (HTTP), 443 (HTTPS)                  │  │
│  │  - React 빌드 파일 서빙                          │  │
│  │  - API 요청 프록시                               │  │
│  └──────┬───────────────────────┬───────────────────┘  │
│         │                       │                       │
│         │ /api/auth             │ /api/chat, /api/shop │
│         ▼                       ▼                       │
│  ┌──────────────┐      ┌──────────────┐              │
│  │ auth-service │      │ chat-service │              │
│  │ :8080        │      │ :8081        │              │
│  │ (Spring Boot)│      │ (Spring Boot)│              │
│  └──────┬───────┘      └──────┬───────┘              │
│         │                     │                       │
│         ▼                     ▼                       │
│  ┌──────────────┐      ┌──────────────┐              │
│  │ auth-db      │      │ chat-db      │              │
│  │ (MariaDB)    │      │ (Redis)      │              │
│  └──────────────┘      └──────────────┘              │
└─────────────────────────────────────────────────────────┘
```

### 요청 흐름

**예시: 로그인 요청**

```
1. 브라우저
   POST http://외부IP:8181/api/auth/login
   ↓
2. Windows 서버 포트 포워딩 (8181 → 80)
   ↓
3. portal-frontend 컨테이너 (Nginx)
   - /api/auth 요청 감지
   - proxy_pass http://auth-service:8080
   ↓
4. auth-service 컨테이너 (Spring Boot)
   - AuthController.login() 처리
   - 사용자 인증
   ↓
5. auth-db 컨테이너 (MariaDB)
   - 사용자 정보 조회
   ↓
6. 응답 반환
   - JWT 토큰을 HttpOnly Cookie로 설정
   - 사용자 정보 반환
```

### 서비스 통신 방식

**현재 (Phase 1): Nginx 프록시 패턴**

```
프론트엔드 → Nginx → 각 서비스
```

- **장점**: 구현 간단, 빠른 시작
- **단점**: 서비스가 늘어나면 Nginx 설정 복잡

**향후 (Phase 2): API Gateway 패턴**

```
프론트엔드 → API Gateway → 각 서비스
```

- **장점**: 중앙 집중식 관리, 확장성 우수

---

## 🛠️ 기술 스택

### 프론트엔드
- **React 19.2.0**: UI 라이브러리
- **TanStack Query**: 서버 상태 관리 (캐싱, 리패칭)
- **Zustand**: 클라이언트 상태 관리
- **Axios**: HTTP 클라이언트
- **React Router**: 라우팅

### 백엔드
- **Spring Boot 3.5.7**: Java 프레임워크
- **MyBatis**: ORM 매퍼
- **Spring Security**: 보안 프레임워크
- **JWT (JJWT)**: 토큰 생성/검증
- **BCrypt**: 비밀번호 암호화

### 데이터베이스
- **MariaDB**: Auth 서비스 (사용자, 권한, 토큰)
- **Redis**: Chat 서비스 (예정)
- **PostgreSQL**: Shop 서비스 (예정)

### 인프라
- **Docker**: 컨테이너화
- **Docker Compose**: 서비스 오케스트레이션
- **Nginx**: 웹 서버 및 리버스 프록시

---

## 📁 프로젝트 구조

```
jh_platform/
├── docker-compose.yml          # 전체 서비스 오케스트레이션
│
├── portal/
│   └── portal_fronted/
│       ├── Dockerfile          # 프론트엔드 빌드 설정
│       ├── nginx.conf          # Nginx 설정 (API 프록시)
│       ├── package.json
│       ├── src/
│       │   ├── api/            # API 호출 함수
│       │   ├── components/     # React 컴포넌트
│       │   ├── hooks/           # 커스텀 훅
│       │   ├── pages/          # 페이지 컴포넌트
│       │   └── store/          # Zustand 스토어
│       └── public/
│
├── auth/
│   ├── 테이블.sql              # DB 초기화 스크립트
│   └── auth/
│       ├── Dockerfile          # 백엔드 빌드 설정
│       ├── build.gradle
│       └── src/
│           └── main/
│               ├── java/
│               │   ├── config/      # 설정 클래스
│               │   ├── controller/  # REST 컨트롤러
│               │   ├── service/     # 비즈니스 로직
│               │   ├── mapper/     # MyBatis 매퍼
│               │   ├── model/      # 엔티티
│               │   ├── dto/        # 데이터 전송 객체
│               │   └── exception/  # 예외 처리
│               └── resources/
│                   ├── application.yml          # 기본 설정
│                   ├── application-prod.yml      # 프로덕션 설정
│                   └── mapper/                   # MyBatis SQL
│
└── docs/
    ├── MASTER_GUIDE.md         # 이 문서 (메인 가이드)
    ├── ARCHITECTURE.md         # 상세 아키텍처
    ├── DEPLOYMENT_GUIDE.md     # 상세 배포 가이드
    ├── ADDING_NEW_PROJECT.md   # 새 프로젝트 추가
    ├── DOMAIN_SETUP.md         # 도메인 설정
    ├── EXTERNAL_ACCESS.md      # 외부 접속 설정
    └── DATABASE_TROUBLESHOOTING.md  # DB 문제 해결
```

---

## 🚀 배포 가이드

### 빠른 시작

```powershell
# 1. 프로젝트 디렉토리로 이동
cd C:\deploy\jh_platform

# 2. 빌드 및 실행
docker-compose up -d --build

# 3. 로그 확인
docker-compose logs -f

# 4. 접속
# http://외부IP:8181
```

### 배포 단계

1. **준비**: 프로젝트 파일 서버로 복사
2. **빌드**: Docker 이미지 생성
3. **실행**: 컨테이너 시작
4. **설정**: CORS, 포트 포워딩 등

**상세 가이드**: [DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md)

### 주요 명령어

```powershell
# 빌드
docker-compose build --no-cache

# 실행
docker-compose up -d

# 중지
docker-compose down

# 로그 확인
docker-compose logs -f auth-service

# 재시작
docker-compose restart auth-service
```

---

## ➕ 새 프로젝트 추가

### 핵심 원칙

**동일한 패턴 반복**: Auth 서비스를 추가했던 것과 동일한 방식

### 추가 단계

1. **프로젝트 디렉토리 생성**
   ```
   chat/chat/
   ├── Dockerfile
   ├── build.gradle
   └── src/
   ```

2. **Docker Compose에 서비스 추가**
   ```yaml
   chat-service:
     build: ./chat/chat
     ports: "8081:8081"
   ```

3. **Nginx 라우팅 추가**
   ```nginx
   location /api/chat {
       proxy_pass http://chat-service:8081;
   }
   ```

4. **빌드 및 실행**
   ```powershell
   docker-compose build chat-service
   docker-compose up -d
   ```

**상세 가이드**: [ADDING_NEW_PROJECT.md](./ADDING_NEW_PROJECT.md)

### 변경 범위

| 작업 | 변경 범위 |
|------|----------|
| Nginx 라우팅 | 1줄 추가 |
| Docker Compose | 서비스 1개 추가 |
| Dockerfile | Auth와 동일한 패턴 |

---

## 🌐 도메인 설정

### 변경 사항 요약

**변경 필요:**
- ✅ Nginx 설정 (도메인, SSL)
- ✅ CORS 설정 (도메인으로 변경)
- ✅ DNS 설정

**변경 불필요:**
- ❌ 소스 코드
- ❌ 서비스 구조

### 단계

1. **DNS 설정**: 도메인 → 서버 IP 매핑
2. **SSL 인증서 발급**: Let's Encrypt (무료)
3. **Nginx 설정**: HTTPS, 도메인 설정
4. **CORS 설정**: 도메인으로 변경

**상세 가이드**: [DOMAIN_SETUP.md](./DOMAIN_SETUP.md)

---

## 🔧 외부 접속 설정

### 현재 상황

- **접속**: `http://외부IP:8181`
- **CORS**: IP 기반 설정

### 설정 방법

**docker-compose.yml**:
```yaml
auth-service:
  environment:
    - CORS_ALLOWED_ORIGIN=http://외부IP:8181
```

**재시작**:
```powershell
docker-compose restart auth-service
```

**상세 가이드**: [EXTERNAL_ACCESS.md](./EXTERNAL_ACCESS.md)

---

## 🐛 문제 해결

### 자주 발생하는 문제

#### 1. 403 Forbidden

**원인**: CORS 설정 문제

**해결**:
```powershell
# 1. CORS 설정 확인
# docker-compose.yml에서 CORS_ALLOWED_ORIGIN 확인

# 2. 백엔드 재시작
docker-compose restart auth-service
```

#### 2. 테이블이 생성되지 않음

**원인**: 볼륨에 이미 데이터가 있음

**해결**:
```powershell
# 볼륨 삭제 후 재시작
docker-compose down -v
docker-compose up -d
```

#### 3. 포트 충돌

**원인**: 포트가 이미 사용 중

**해결**:
```powershell
# 포트 확인
netstat -ano | findstr :80

# docker-compose.yml에서 포트 변경
ports:
  - "8081:80"
```

#### 4. 빌드 실패

**원인**: 의존성 문제, 네트워크 문제

**해결**:
```powershell
# 캐시 없이 재빌드
docker-compose build --no-cache
```

**상세 가이드**: [DATABASE_TROUBLESHOOTING.md](./DATABASE_TROUBLESHOOTING.md)

---

## ❓ FAQ

### Q1: 새 프로젝트 추가 시 많이 변경해야 하나?

**A**: 아니요. Nginx 라우팅 1줄, Docker Compose 서비스 1개만 추가하면 됩니다.

### Q2: 도메인 추가 시 많이 변경해야 하나?

**A**: 아니요. 설정 파일만 수정하면 됩니다 (Nginx, Docker Compose, CORS).

### Q3: 각 프로젝트는 독립적인 DB를 사용하나요?

**A**: 네. 각 프로젝트마다 독립적인 데이터베이스를 사용할 수 있습니다 (MariaDB, Redis, PostgreSQL 등).

### Q4: 권한은 어떻게 관리하나요?

**A**: 각 프로젝트마다 독립적인 권한 구조를 가집니다. 예: `PORTAL_ADMIN`, `CHAT_USER`, `SHOP_ADMIN` 등.

### Q5: 서비스 간 통신은 어떻게 하나요?

**A**: 현재는 Nginx를 통한 프록시 방식입니다. 향후 API Gateway로 전환 가능합니다.

### Q6: 로컬 개발과 배포 환경의 차이는?

**A**: 
- **로컬**: 각 서비스를 개별적으로 실행 (npm start, ./gradlew bootRun)
- **배포**: Docker Compose로 모든 서비스를 한 번에 실행

---

## 📚 상세 문서 링크

### 아키텍처 및 설계
- [ARCHITECTURE.md](./ARCHITECTURE.md) - 시스템 아키텍처 상세 설명

### 배포 관련
- [DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md) - 배포 완전 가이드
- [EXTERNAL_ACCESS.md](./EXTERNAL_ACCESS.md) - 외부 접속 설정
- [DOMAIN_SETUP.md](./DOMAIN_SETUP.md) - 도메인 및 SSL 설정

### 확장 관련
- [ADDING_NEW_PROJECT.md](./ADDING_NEW_PROJECT.md) - 새 프로젝트 추가 가이드

### 문제 해결
- [DATABASE_TROUBLESHOOTING.md](./DATABASE_TROUBLESHOOTING.md) - 데이터베이스 문제 해결

### 인증 시스템
- [2025-11-01/AUTH_SYSTEM_DOCUMENTATION.md](./2025-11-01/AUTH_SYSTEM_DOCUMENTATION.md) - 인증 시스템 상세

---

## 🔄 업데이트 이력

### 2024-11-29
- ✅ Portal 대시보드 구현
- ✅ 관리자 페이지 구현
- ✅ 프로젝트 목록 표시
- ✅ Docker 배포 설정 완료
- ✅ 외부 접속 설정 완료
- ✅ 문서 통합 완료

### 향후 계획
- ⏳ Chat 서비스 추가
- ⏳ Shop 서비스 추가
- ⏳ API Gateway 도입
- ⏳ 권한 관리 UI

---

## 🎯 빠른 참조

### 배포 명령어

```powershell
# 전체 빌드 및 실행
docker-compose up -d --build

# 특정 서비스만 재빌드
docker-compose build --no-cache portal-frontend

# 로그 확인
docker-compose logs -f auth-service

# 서비스 재시작
docker-compose restart auth-service

# 서비스 중지 및 삭제
docker-compose down -v
```

### 데이터베이스 접속

```powershell
# MariaDB 접속
docker exec -it auth-db mysql -u authuser -pauthpass authdb

# 테이블 확인
docker exec -it auth-db mysql -u authuser -pauthpass authdb -e "SHOW TABLES;"
```

### 포트 매핑

| 서비스 | 내부 포트 | 외부 포트 | 접속 URL |
|--------|----------|----------|----------|
| portal-frontend | 80 | 80 (또는 8181) | http://외부IP:8181 |
| auth-service | 8080 | 8080 | http://외부IP:8080 |
| auth-db | 3306 | 3306 | localhost:3306 |

---

## 📝 문서 업데이트 가이드

이 문서는 프로젝트와 함께 지속적으로 업데이트됩니다.

### 업데이트 시 포함할 내용

1. **새로운 기능 추가 시**
   - 기능 설명
   - 사용 방법
   - 설정 변경사항

2. **새 프로젝트 추가 시**
   - 프로젝트 정보
   - 포트 할당
   - 라우팅 설정

3. **문제 해결 시**
   - 문제 원인
   - 해결 방법
   - 예방 방법

4. **아키텍처 변경 시**
   - 변경 내용
   - 영향 범위
   - 마이그레이션 가이드

---

## 🎓 학습 자료

### Docker
- [Docker 공식 문서](https://docs.docker.com/)
- [Docker Compose 문서](https://docs.docker.com/compose/)

### Spring Boot
- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [Spring Security 가이드](https://spring.io/guides/topicals/spring-security-architecture)

### React
- [React 공식 문서](https://react.dev/)
- [TanStack Query 문서](https://tanstack.com/query/latest)

---

## 📞 지원

문제가 발생하거나 질문이 있으면:
1. 관련 문서 확인
2. 로그 확인 (`docker-compose logs`)
3. 문제 해결 가이드 참고

---

**이 문서는 프로젝트의 모든 내용을 종합한 메인 가이드입니다. 지속적으로 업데이트됩니다.**

