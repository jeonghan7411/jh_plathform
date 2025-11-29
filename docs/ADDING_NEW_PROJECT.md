# 새 프로젝트 추가 가이드

## 📋 목차

1. [개요](#개요)
2. [프로젝트 추가 단계](#프로젝트-추가-단계)
3. [Nginx 라우팅 설정](#nginx-라우팅-설정)
4. [Docker Compose 설정](#docker-compose-설정)
5. [실전 예제: Chat 서비스 추가](#실전-예제-chat-서비스-추가)
6. [체크리스트](#체크리스트)

---

## 🎯 개요

### 현재 구조

```
portal-frontend (Nginx)
  ├─ /api/auth/* → auth-service:8080
  └─ / (정적 파일 서빙)
```

### 새 프로젝트 추가 후 구조

```
portal-frontend (Nginx)
  ├─ /api/auth/* → auth-service:8080
  ├─ /api/chat/* → chat-service:8081  [새로 추가]
  ├─ /api/shop/* → shop-service:8082  [새로 추가]
  └─ / (정적 파일 서빙)
```

### 핵심 원칙

1. **각 프로젝트는 독립적인 서비스**
   - 독립적인 컨테이너
   - 독립적인 포트
   - 독립적인 데이터베이스 (선택)

2. **Nginx가 단일 진입점**
   - 모든 API 요청은 `/api/{project}/*` 형식
   - Nginx가 적절한 서비스로 라우팅

3. **동일한 패턴 반복**
   - Auth 서비스와 동일한 방식으로 추가

---

## 🚀 프로젝트 추가 단계

### Step 1: 프로젝트 디렉토리 구조 생성

```
jh_platform/
├── chat/                    # 새 프로젝트
│   └── chat/
│       ├── Dockerfile
│       ├── build.gradle
│       └── src/
│
└── shop/                    # 또 다른 프로젝트
    └── shop/
        ├── Dockerfile
        ├── build.gradle
        └── src/
```

### Step 2: Dockerfile 생성

**chat/chat/Dockerfile** (Auth와 동일한 패턴)

```dockerfile
# Gradle 빌드 단계
FROM gradle:8-jdk21 AS build

WORKDIR /app

# Gradle 설정 파일 복사
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# 의존성 다운로드
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사
COPY src ./src

# 애플리케이션 빌드
RUN gradle build -x test --no-daemon

# 실행 단계
FROM eclipse-temurin:21-jdk

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8081

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Step 3: application-prod.yml 생성

**chat/chat/src/main/resources/application-prod.yml**

```yaml
spring:
  datasource:
    url: jdbc:redis://chat-db:6379  # 또는 다른 DB
    # 또는 PostgreSQL: jdbc:postgresql://chat-db:5432/chatdb
  application:
    name: chat

server:
  port: 8081

# CORS 설정
cors:
  allowed-origins:
    - ${CORS_ALLOWED_ORIGIN:http://localhost}
  allowed-methods:
    - GET
    - POST
    - PUT
    - DELETE
    - OPTIONS
  allowed-headers: "*"
  allow-credentials: true
  max-age: 3600
```

### Step 4: Docker Compose에 서비스 추가

**docker-compose.yml**에 추가:

```yaml
services:
  # 기존 서비스들...
  
  # Chat 서비스 (새로 추가)
  chat-service:
    build:
      context: ./chat/chat
      dockerfile: Dockerfile
    container_name: chat-service
    ports:
      - "8081:8081"  # 외부에서 접근 필요 시
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:redis://chat-db:6379
      - CORS_ALLOWED_ORIGIN=${CORS_ALLOWED_ORIGIN:-http://localhost}
    depends_on:
      - chat-db
    networks:
      - jh-platform-network
    restart: unless-stopped

  # Chat 서비스 DB (Redis 예시)
  chat-db:
    image: redis:7-alpine
    container_name: chat-db
    ports:
      - "6379:6379"  # 외부에서 접근 필요 시
    networks:
      - jh-platform-network
    restart: unless-stopped
    # Redis 설정
    command: redis-server --appendonly yes
    volumes:
      - chat-db-data:/data

volumes:
  # 기존 볼륨들...
  chat-db-data:
    driver: local
```

### Step 5: Nginx 라우팅 설정

**portal/portal_fronted/nginx.conf** 수정:

```nginx
server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;

    # React Router를 위한 설정
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 정적 파일 캐싱
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # Auth API 프록시
    location /api/auth {
        proxy_pass http://auth-service:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $host;
        proxy_cookie_path / /;
        proxy_cache_bypass $http_upgrade;
    }

    # Chat API 프록시 (새로 추가)
    location /api/chat {
        proxy_pass http://chat-service:8081;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $host;
        proxy_cookie_path / /;
        proxy_cache_bypass $http_upgrade;
    }

    # Shop API 프록시 (예시)
    location /api/shop {
        proxy_pass http://shop-service:8082;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $host;
        proxy_cookie_path / /;
        proxy_cache_bypass $http_upgrade;
    }

    # 보안 헤더
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
}
```

---

## 🔧 Nginx 라우팅 설정

### 라우팅 규칙

**패턴**: `/api/{project-name}/*` → `{project-name}-service:{port}`

| 요청 경로 | 프록시 대상 | 설명 |
|----------|-----------|------|
| `/api/auth/*` | `auth-service:8080` | 인증 서비스 |
| `/api/chat/*` | `chat-service:8081` | 채팅 서비스 |
| `/api/shop/*` | `shop-service:8082` | 쇼핑 서비스 |

### 주의사항

1. **경로 매칭 순서**
   - 더 구체적인 경로가 먼저 매칭됨
   - `/api/auth`가 `/api`보다 먼저 매칭

2. **proxy_pass 설정**
   - `proxy_pass http://chat-service:8081;` (끝에 슬래시 없음)
   - `/api/chat/message` → `http://chat-service:8081/api/chat/message`
   - 만약 `proxy_pass http://chat-service:8081/;` (슬래시 있음)
   - `/api/chat/message` → `http://chat-service:8081/message` (경로 변경됨)

3. **쿠키 전달**
   - `proxy_cookie_path / /;` 필수
   - HttpOnly Cookie 전달을 위해 필요

---

## 📦 Docker Compose 설정

### 포트 할당 전략

각 프로젝트마다 고유한 포트 사용:

| 서비스 | 내부 포트 | 외부 포트 (선택) | 설명 |
|--------|----------|----------------|------|
| portal-frontend | 80 | 80 | Nginx |
| auth-service | 8080 | 8080 | 인증 |
| chat-service | 8081 | 8081 | 채팅 |
| shop-service | 8082 | 8082 | 쇼핑 |

**외부 포트 노출은 선택사항**
- Nginx를 통해서만 접근하는 경우 외부 포트 노출 불필요
- 디버깅 목적으로만 외부 포트 노출

### 네트워크

모든 서비스는 같은 네트워크에 연결:

```yaml
networks:
  jh-platform-network:
    driver: bridge
```

이렇게 하면 서비스 이름으로 서로 통신 가능:
- `auth-service:8080`
- `chat-service:8081`

---

## 💡 실전 예제: Chat 서비스 추가

### 1. 디렉토리 생성

```powershell
mkdir -p chat/chat/src/main/java
mkdir -p chat/chat/src/main/resources
```

### 2. Dockerfile 생성

`chat/chat/Dockerfile` (Auth와 동일)

### 3. build.gradle 생성

`chat/chat/build.gradle` (필요한 의존성 추가)

### 4. Spring Boot 애플리케이션 생성

```java
// ChatApplication.java
@SpringBootApplication
public class ChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }
}
```

### 5. Controller 생성

```java
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    
    @GetMapping("/messages")
    public ResponseEntity<?> getMessages() {
        // 채팅 메시지 조회
        return ResponseEntity.ok("Messages");
    }
    
    @PostMapping("/messages")
    public ResponseEntity<?> sendMessage() {
        // 채팅 메시지 전송
        return ResponseEntity.ok("Message sent");
    }
}
```

### 6. docker-compose.yml에 추가

```yaml
services:
  chat-service:
    build:
      context: ./chat/chat
      dockerfile: Dockerfile
    container_name: chat-service
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - CORS_ALLOWED_ORIGIN=${CORS_ALLOWED_ORIGIN:-http://localhost}
    networks:
      - jh-platform-network
    restart: unless-stopped

  chat-db:
    image: redis:7-alpine
    container_name: chat-db
    networks:
      - jh-platform-network
    restart: unless-stopped
    volumes:
      - chat-db-data:/data

volumes:
  chat-db-data:
    driver: local
```

### 7. nginx.conf에 라우팅 추가

```nginx
location /api/chat {
    proxy_pass http://chat-service:8081;
    # ... (나머지 설정은 위 예제 참고)
}
```

### 8. 빌드 및 실행

```powershell
# 새 서비스 빌드
docker-compose build chat-service

# 모든 서비스 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f chat-service
```

---

## ✅ 체크리스트

### 새 프로젝트 추가 시 확인사항

- [ ] 프로젝트 디렉토리 구조 생성
- [ ] Dockerfile 생성 (Auth와 동일한 패턴)
- [ ] build.gradle 설정
- [ ] application-prod.yml 생성
- [ ] Controller에서 `/api/{project-name}` 경로 사용
- [ ] docker-compose.yml에 서비스 추가
- [ ] 포트 할당 (중복 없음)
- [ ] DB 컨테이너 추가 (필요 시)
- [ ] nginx.conf에 라우팅 추가
- [ ] CORS 설정 확인
- [ ] 프론트엔드 재빌드 (nginx.conf 변경 시)
- [ ] 테스트: `http://외부IP:8181/api/{project-name}/...`

---

## 🔄 업데이트 프로세스

### 기존 서비스 업데이트

```powershell
# 1. 코드 변경

# 2. 특정 서비스만 재빌드
docker-compose build --no-cache chat-service

# 3. 재시작
docker-compose up -d chat-service

# 4. 로그 확인
docker-compose logs -f chat-service
```

### Nginx 설정 변경 시

```powershell
# 1. nginx.conf 수정

# 2. 프론트엔드 재빌드
docker-compose build --no-cache portal-frontend

# 3. 재시작
docker-compose up -d portal-frontend
```

---

## 🎯 핵심 정리

### 질문: 다른 프로젝트 추가 시 똑같은 방식으로 진행?

**답변: 네, 맞습니다!**

1. **동일한 패턴 반복**
   - Auth 서비스와 동일한 방식
   - Dockerfile, docker-compose.yml, nginx.conf만 추가

2. **Nginx에서 포트 조정만?**

**아니요, 더 정확히는:**
- ✅ Nginx에서 **라우팅 경로 추가** (`/api/chat` → `chat-service:8081`)
- ✅ Docker Compose에서 **새 서비스 추가** (포트, 환경 변수 등)
- ✅ 각 프로젝트의 **Dockerfile 생성** (동일한 패턴)

### 요약

```
새 프로젝트 추가 = 
  Auth 서비스 추가했던 것과 동일한 과정
  + Nginx 라우팅 1줄 추가
  + docker-compose.yml에 서비스 1개 추가
```

---

## 📚 참고

- [DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md) - 기본 배포 가이드
- [ARCHITECTURE.md](./ARCHITECTURE.md) - 전체 아키텍처

---

이 가이드를 따라하면 새로운 프로젝트를 쉽게 추가할 수 있습니다!

