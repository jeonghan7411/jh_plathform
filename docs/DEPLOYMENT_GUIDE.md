# 배포 완전 가이드

## 📋 목차

1. [배포 구조 이해](#배포-구조-이해)
2. [Docker 기본 개념](#docker-기본-개념)
3. [프로젝트 구조](#프로젝트-구조)
4. [배포 단계별 과정](#배포-단계별-과정)
5. [설정 파일 설명](#설정-파일-설명)
6. [외부 접속 설정](#외부-접속-설정)
7. [문제 해결](#문제-해결)

---

## 🏗️ 배포 구조 이해

### 전체 아키텍처

```
┌─────────────────────────────────────────────────┐
│         사용자 브라우저                          │
│    http://외부IP:8181 접속                       │
└──────────────────┬──────────────────────────────┘
                   │
                   │ HTTP 요청
                   ▼
┌─────────────────────────────────────────────────┐
│    Windows 서버                                  │
│                                                  │
│  ┌──────────────────────────────────────────┐  │
│  │  Docker 컨테이너들                        │  │
│  │                                            │  │
│  │  ┌────────────────────────────────────┐  │  │
│  │  │ portal-frontend (Nginx)             │  │  │
│  │  │ - 포트: 80 (내부)                   │  │  │
│  │  │ - React 빌드 파일 서빙              │  │  │
│  │  │ - /api 요청을 auth-service로 프록시 │  │  │
│  │  └──────────┬─────────────────────────┘  │  │
│  │             │                              │  │
│  │             │ /api 요청                    │  │
│  │             ▼                              │  │
│  │  ┌────────────────────────────────────┐  │  │
│  │  │ auth-service (Spring Boot)          │  │  │
│  │  │ - 포트: 8080 (내부)                 │  │  │
│  │  │ - 인증/인가 API 처리                │  │  │
│  │  └──────────┬─────────────────────────┘  │  │
│  │             │                              │  │
│  │             │ DB 연결                      │  │
│  │             ▼                              │  │
│  │  ┌────────────────────────────────────┐  │  │
│  │  │ auth-db (MariaDB)                  │  │  │
│  │  │ - 포트: 3306 (내부)                 │  │  │
│  │  │ - 사용자 데이터 저장                │  │  │
│  │  └────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────┘  │
│                                                  │
│  포트 매핑:                                      │
│  - 외부 8181 → 내부 80 (portal-frontend)        │
│  - 외부 8080 → 내부 8080 (auth-service)         │
│  - 외부 3306 → 내부 3306 (auth-db)              │
└─────────────────────────────────────────────────┘
```

### 요청 흐름 예시

**1. 사용자가 로그인 요청**
```
브라우저 → http://외부IP:8181/api/auth/login
  ↓
Windows 서버의 포트 포워딩 (8181 → 80)
  ↓
portal-frontend 컨테이너 (Nginx:80)
  ↓
Nginx가 /api 요청을 감지
  ↓
proxy_pass http://auth-service:8080
  ↓
auth-service 컨테이너 (Spring Boot:8080)
  ↓
AuthController.login() 처리
  ↓
auth-db 컨테이너에서 사용자 정보 조회
  ↓
응답 반환 (JWT 토큰을 쿠키로 설정)
```

---

## 🐳 Docker 기본 개념

### Docker란?

**컨테이너 기반 가상화 기술**
- 애플리케이션과 그 실행 환경을 하나의 패키지로 묶음
- 서버에 설치된 Docker만 있으면 어디서든 동일하게 실행 가능

### 주요 개념

#### 1. 이미지 (Image)
- **정의**: 애플리케이션과 실행 환경이 포함된 템플릿
- **예시**: `nginx:alpine`, `mariadb:10.11`
- **생성**: Dockerfile로 빌드하여 생성

#### 2. 컨테이너 (Container)
- **정의**: 이미지를 실행한 인스턴스
- **특징**: 독립적인 실행 환경, 격리된 네트워크/파일시스템
- **생명주기**: 시작 → 실행 → 중지 → 삭제

#### 3. Docker Compose
- **정의**: 여러 컨테이너를 한 번에 관리하는 도구
- **파일**: `docker-compose.yml`
- **장점**: 서비스 간 의존성, 네트워크, 볼륨을 한 번에 관리

### Docker 명령어 기본

```powershell
# 이미지 목록
docker images

# 컨테이너 목록 (실행 중)
docker ps

# 컨테이너 목록 (모두)
docker ps -a

# 컨테이너 로그 확인
docker logs 컨테이너이름

# 컨테이너 내부 접속
docker exec -it 컨테이너이름 bash
```

### Docker Compose 명령어

```powershell
# 모든 서비스 빌드 및 실행
docker-compose up -d

# 특정 서비스만 빌드
docker-compose build 서비스이름

# 서비스 중지
docker-compose stop

# 서비스 중지 및 컨테이너 삭제
docker-compose down

# 로그 확인
docker-compose logs -f

# 서비스 재시작
docker-compose restart
```

---

## 📁 프로젝트 구조

```
jh_platform/
├── docker-compose.yml          # 전체 서비스 오케스트레이션
│
├── portal/
│   └── portal_fronted/
│       ├── Dockerfile          # 프론트엔드 빌드 설정
│       ├── nginx.conf          # Nginx 설정 (API 프록시 포함)
│       ├── package.json        # Node.js 의존성
│       ├── src/                # React 소스 코드
│       └── public/             # 정적 파일
│
└── auth/
    ├── 테이블.sql              # DB 초기화 스크립트
    └── auth/
        ├── Dockerfile          # 백엔드 빌드 설정
        ├── build.gradle        # Gradle 빌드 설정
        ├── src/
        │   └── main/
        │       ├── java/       # Spring Boot 소스
        │       └── resources/
        │           ├── application.yml          # 기본 설정
        │           ├── application-prod.yml      # 프로덕션 설정
        │           └── mapper/                  # MyBatis SQL
        └── gradle/             # Gradle Wrapper
```

---

## 🚀 배포 단계별 과정

### Phase 1: 준비 단계

#### 1-1. 프로젝트 파일 준비

```powershell
# 방법 1: 전체 디렉토리 복사
# jh_platform 디렉토리 전체를 서버로 복사

# 방법 2: Git 사용
git clone <repository-url> jh_platform
cd jh_platform
```

#### 1-2. Docker 설치 확인

```powershell
# Docker 설치 확인
docker --version
docker-compose --version

# Docker Desktop 실행 확인
# (Windows 서버에서 Docker Desktop이 실행 중이어야 함)
```

---

### Phase 2: 빌드 단계

#### 2-1. 이미지 빌드

```powershell
# 프로젝트 디렉토리로 이동
cd C:\deploy\jh_platform

# 모든 서비스 빌드 (캐시 사용)
docker-compose build

# 또는 캐시 없이 완전히 새로 빌드
docker-compose build --no-cache
```

**빌드 과정:**

1. **portal-frontend 빌드**
   ```
   - Node.js 20 이미지 다운로드
   - package.json 복사
   - npm install (의존성 설치)
   - 소스 코드 복사
   - npm run build (React 앱 빌드)
   - Nginx 이미지 다운로드
   - 빌드된 파일을 Nginx에 복사
   → 이미지 생성: jh-platform_portal-frontend
   ```

2. **auth-service 빌드**
   ```
   - Gradle 이미지 다운로드
   - build.gradle 복사
   - 의존성 다운로드
   - 소스 코드 복사
   - gradle build (JAR 파일 생성)
   - Eclipse Temurin 이미지 다운로드
   - JAR 파일 복사
   → 이미지 생성: jh-platform_auth-service
   ```

3. **auth-db**
   ```
   - MariaDB 이미지 다운로드 (또는 기존 이미지 사용)
   → 이미지 사용: mariadb:10.11
   ```

#### 2-2. 빌드 결과 확인

```powershell
# 생성된 이미지 확인
docker images

# 예상 결과:
# jh-platform_portal-frontend    latest    ...
# jh-platform_auth-service       latest    ...
# mariadb                         10.11     ...
```

---

### Phase 3: 실행 단계

#### 3-1. 컨테이너 시작

```powershell
# 모든 서비스 시작 (백그라운드)
docker-compose up -d

# 또는 빌드와 함께 시작
docker-compose up -d --build
```

**실행 과정:**

1. **네트워크 생성**
   - `jh-platform-network` 네트워크 생성
   - 모든 컨테이너가 이 네트워크에 연결

2. **볼륨 생성**
   - `auth-db-data` 볼륨 생성 (DB 데이터 저장)

3. **컨테이너 시작**
   - `auth-db` 시작 → MariaDB 초기화 → `테이블.sql` 실행
   - `auth-service` 시작 → Spring Boot 애플리케이션 실행
   - `portal-frontend` 시작 → Nginx 서버 실행

#### 3-2. 실행 상태 확인

```powershell
# 컨테이너 상태 확인
docker-compose ps

# 예상 결과:
# NAME                STATUS          PORTS
# portal-frontend     Up 10 minutes   0.0.0.0:80->80/tcp
# auth-service        Up 10 minutes   0.0.0.0:8080->8080/tcp
# auth-db             Up 10 minutes   0.0.0.0:3306->3306/tcp
```

#### 3-3. 로그 확인

```powershell
# 모든 서비스 로그
docker-compose logs -f

# 특정 서비스 로그
docker-compose logs -f auth-service
docker-compose logs -f portal-frontend
docker-compose logs -f auth-db
```

---

### Phase 4: 외부 접속 설정

#### 4-1. 포트 포워딩 설정

Windows 서버에서 외부 접속을 위해 포트 포워딩이 필요합니다.

**현재 설정:**
- Docker 컨테이너: 내부 포트 80
- 외부 접속: 외부 IP:8181

**포트 포워딩 방법:**
1. Windows 방화벽 설정
2. 라우터 포트 포워딩 (필요 시)
3. 또는 Docker 포트 매핑 변경

#### 4-2. CORS 설정

외부 IP:8181로 접속하는 경우, 백엔드 CORS 설정 필요:

**docker-compose.yml 수정:**
```yaml
auth-service:
  environment:
    - CORS_ALLOWED_ORIGIN=http://외부IP:8181
    # 예: - CORS_ALLOWED_ORIGIN=http://116.125.170.79:8181
```

**재시작:**
```powershell
docker-compose restart auth-service
```

---

## ⚙️ 설정 파일 설명

### 1. docker-compose.yml

**역할**: 전체 서비스 오케스트레이션

**주요 설정:**

```yaml
services:
  portal-frontend:
    build: ./portal/portal_fronted    # 빌드 경로
    ports: "80:80"                    # 포트 매핑 (호스트:컨테이너)
    depends_on: auth-service          # 의존성 (auth-service 먼저 시작)
    networks: jh-platform-network     # 네트워크

  auth-service:
    environment:                      # 환경 변수
      - SPRING_PROFILES_ACTIVE=prod   # 프로덕션 프로파일
      - CORS_ALLOWED_ORIGIN=...        # CORS 설정

  auth-db:
    volumes:                          # 볼륨 마운트
      - auth-db-data:/var/lib/mysql   # 데이터 영구 저장
      - ./auth/테이블.sql:/docker-entrypoint-initdb.d/init.sql  # 초기화 스크립트
```

### 2. Dockerfile (프론트엔드)

**역할**: React 앱을 빌드하고 Nginx로 서빙

**단계:**
1. Node.js로 React 앱 빌드
2. Nginx 이미지에 빌드된 파일 복사
3. Nginx 설정 파일 복사

### 3. Dockerfile (백엔드)

**역할**: Spring Boot 앱을 빌드하고 실행

**단계:**
1. Gradle로 JAR 파일 빌드
2. Java 런타임 이미지에 JAR 파일 복사
3. Java로 JAR 실행

### 4. nginx.conf

**역할**: Nginx 서버 설정

**주요 설정:**
- 정적 파일 서빙 (React 빌드 파일)
- API 프록시 (`/api` → `auth-service:8080`)
- React Router 지원

### 5. application-prod.yml

**역할**: 프로덕션 환경 설정

**주요 설정:**
- 데이터베이스 연결 정보
- JWT 설정
- CORS 설정
- 로깅 레벨

---

## 🌐 외부 접속 설정

### 현재 상황

- **내부**: Docker 컨테이너들이 80, 8080, 3306 포트로 실행
- **외부**: 사용자는 `외부IP:8181`로 접속

### 요청 흐름

```
사용자 브라우저
  ↓
http://외부IP:8181 접속
  ↓
Windows 서버 포트 포워딩 (8181 → 80)
  ↓
portal-frontend 컨테이너 (Nginx:80)
  ↓
/api 요청 → auth-service:8080으로 프록시
  ↓
auth-service 처리
```

### CORS 설정

**문제**: 브라우저가 다른 Origin에서 요청하면 CORS 에러 발생

**해결**: 백엔드에서 외부 IP:8181을 허용하도록 설정

```yaml
# docker-compose.yml
auth-service:
  environment:
    - CORS_ALLOWED_ORIGIN=http://외부IP:8181
```

---

## 🔧 문제 해결

### 문제 1: 403 Forbidden

**원인**: CORS 설정 문제 또는 Spring Security 설정 문제

**해결**:
1. `SecurityConfig.java`에서 `.cors()` 활성화 확인
2. `docker-compose.yml`에서 `CORS_ALLOWED_ORIGIN` 설정 확인
3. 백엔드 재시작: `docker-compose restart auth-service`

### 문제 2: 테이블이 생성되지 않음

**원인**: 볼륨에 이미 데이터가 있으면 초기화 스크립트 실행 안 됨

**해결**:
```powershell
# 볼륨 삭제 후 재시작
docker-compose down -v
docker-compose up -d
```

### 문제 3: 포트 충돌

**원인**: 포트가 이미 사용 중

**해결**:
```powershell
# 포트 사용 확인
netstat -ano | findstr :80

# docker-compose.yml에서 포트 변경
ports:
  - "8081:80"  # 호스트 포트 변경
```

### 문제 4: 빌드 실패

**원인**: 의존성 문제, 네트워크 문제 등

**해결**:
```powershell
# 캐시 없이 재빌드
docker-compose build --no-cache

# 특정 서비스만 재빌드
docker-compose build --no-cache portal-frontend
```

---

## 📝 배포 체크리스트

### 배포 전

- [ ] Docker Desktop/Engine 설치 확인
- [ ] 프로젝트 파일 서버로 복사
- [ ] 포트 80, 8080, 3306 사용 가능 확인
- [ ] 외부 IP 확인

### 빌드

- [ ] `docker-compose build` 성공
- [ ] 이미지 생성 확인 (`docker images`)

### 실행

- [ ] `docker-compose up -d` 성공
- [ ] 모든 컨테이너 실행 중 (`docker-compose ps`)
- [ ] 로그에 에러 없음 (`docker-compose logs`)

### 설정

- [ ] CORS 설정 (외부 IP 추가)
- [ ] 포트 포워딩 설정 (8181 → 80)
- [ ] 데이터베이스 테이블 생성 확인

### 테스트

- [ ] 브라우저에서 접속 가능
- [ ] 로그인/회원가입 동작 확인
- [ ] API 요청 성공 확인

---

## 🎯 핵심 개념 정리

### Docker 이미지 vs 컨테이너

- **이미지**: 템플릿 (코드 + 실행 환경)
- **컨테이너**: 이미지를 실행한 인스턴스

### 빌드 vs 실행

- **빌드**: 소스 코드 → Docker 이미지 생성
- **실행**: 이미지 → 컨테이너 시작

### 포트 매핑

- **형식**: `호스트포트:컨테이너포트`
- **예시**: `"80:80"` → 호스트 80 포트 = 컨테이너 80 포트

### 볼륨 (Volume)

- **역할**: 데이터 영구 저장
- **예시**: DB 데이터는 볼륨에 저장되어 컨테이너 삭제해도 유지

### 네트워크

- **역할**: 컨테이너 간 통신
- **예시**: `portal-frontend` → `auth-service` 통신

---

## 🚀 빠른 시작 가이드

```powershell
# 1. 프로젝트 디렉토리로 이동
cd C:\deploy\jh_platform

# 2. 빌드 및 실행
docker-compose up -d --build

# 3. 로그 확인
docker-compose logs -f

# 4. 상태 확인
docker-compose ps

# 5. 브라우저에서 접속
# http://외부IP:8181
```

---

## 📚 추가 학습 자료

- [Docker 공식 문서](https://docs.docker.com/)
- [Docker Compose 문서](https://docs.docker.com/compose/)
- [Spring Boot 배포 가이드](https://spring.io/guides/gs/spring-boot-docker/)

---

이 문서를 통해 배포 과정을 이해하고 문제를 해결할 수 있습니다!

