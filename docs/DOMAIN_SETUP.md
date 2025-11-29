# 도메인 설정 가이드

## 📋 목차

1. [개요](#개요)
2. [도메인 추가 시 변경사항](#도메인-추가-시-변경사항)
3. [단계별 설정](#단계별-설정)
4. [SSL 인증서 설정 (HTTPS)](#ssl-인증서-설정-https)
5. [DNS 설정](#dns-설정)
6. [체크리스트](#체크리스트)

---

## 🎯 개요

### 현재 상황

- **접속**: `http://외부IP:8181`
- **프로토콜**: HTTP (비보안)
- **CORS**: IP 기반 설정

### 도메인 추가 후

- **접속**: `https://yourdomain.com`
- **프로토콜**: HTTPS (보안)
- **CORS**: 도메인 기반 설정

### 변경 범위

**변경이 필요한 부분:**
- ✅ Nginx 설정 (도메인, SSL)
- ✅ CORS 설정 (도메인으로 변경)
- ✅ DNS 설정 (도메인 → IP 매핑)
- ✅ 환경 변수 (도메인 URL)

**변경이 불필요한 부분:**
- ❌ Docker Compose 구조
- ❌ 각 서비스의 코드
- ❌ 데이터베이스 구조

---

## 🔄 도메인 추가 시 변경사항

### 변경 사항 요약

| 항목 | 현재 | 변경 후 | 변경 범위 |
|------|------|---------|----------|
| **접속 URL** | `http://IP:8181` | `https://yourdomain.com` | 설정 파일만 |
| **Nginx 설정** | IP 기반 | 도메인 + SSL | nginx.conf 수정 |
| **CORS 설정** | IP:8181 | 도메인 | 환경 변수만 |
| **DNS** | 없음 | 도메인 → IP | DNS 서버 설정 |

**결론: 코드 변경은 거의 없고, 설정 파일만 수정하면 됩니다!**

---

## 📝 단계별 설정

### Step 1: DNS 설정

도메인을 구입한 후 DNS 서버에서 설정:

```
타입: A 레코드
이름: @ (또는 www)
값: 서버의 외부 IP 주소
TTL: 3600 (또는 기본값)
```

**예시:**
```
yourdomain.com → 116.125.170.79
www.yourdomain.com → 116.125.170.79
```

**확인 방법:**
```powershell
# DNS 전파 확인
nslookup yourdomain.com
ping yourdomain.com
```

---

### Step 2: SSL 인증서 발급

HTTPS를 사용하려면 SSL 인증서가 필요합니다.

#### 방법 1: Let's Encrypt (무료, 권장)

**Certbot 사용:**

```powershell
# Certbot 설치 (Windows 서버에서)
# 또는 Docker로 실행

# SSL 인증서 발급
certbot certonly --standalone -d yourdomain.com -d www.yourdomain.com
```

**인증서 위치:**
```
/etc/letsencrypt/live/yourdomain.com/
  ├── fullchain.pem  (인증서 + 중간 인증서)
  └── privkey.pem    (개인 키)
```

#### 방법 2: 상용 SSL 인증서

- 도메인 구입 시 제공되는 SSL 인증서 사용
- 또는 상용 인증서 구매

---

### Step 3: Nginx 설정 변경

**portal/portal_fronted/nginx.conf** 수정:

```nginx
# HTTP → HTTPS 리다이렉트
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;
    
    # HTTP 요청을 HTTPS로 리다이렉트
    return 301 https://$server_name$request_uri;
}

# HTTPS 서버 설정
server {
    listen 443 ssl http2;
    server_name yourdomain.com www.yourdomain.com;
    
    # SSL 인증서 설정
    ssl_certificate /etc/nginx/ssl/fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/privkey.pem;
    
    # SSL 보안 설정
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;
    
    root /usr/share/nginx/html;
    index index.html;

    # Gzip 압축
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript application/x-javascript application/xml+rss application/json application/javascript;

    # React Router를 위한 설정
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 정적 파일 캐싱
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # API 프록시 설정
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

    # 보안 헤더
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
}
```

---

### Step 4: Docker Compose 설정 변경

**docker-compose.yml** 수정:

```yaml
services:
  portal-frontend:
    build:
      context: ./portal/portal_fronted
      dockerfile: Dockerfile
    container_name: portal-frontend
    ports:
      - "80:80"      # HTTP (HTTPS 리다이렉트용)
      - "443:443"    # HTTPS
    volumes:
      # SSL 인증서 마운트
      - ./ssl:/etc/nginx/ssl:ro
    # ... 나머지 설정
```

**SSL 인증서 디렉토리 구조:**
```
jh_platform/
├── ssl/
│   ├── fullchain.pem
│   └── privkey.pem
└── docker-compose.yml
```

---

### Step 5: CORS 설정 변경

**docker-compose.yml** 수정:

```yaml
auth-service:
  environment:
    # 도메인으로 변경
    - CORS_ALLOWED_ORIGIN=https://yourdomain.com
    # 또는 여러 도메인 허용
    # - CORS_ALLOWED_ORIGIN=https://yourdomain.com,https://www.yourdomain.com
```

**application-prod.yml**도 확인:

```yaml
cors:
  allowed-origins:
    - ${CORS_ALLOWED_ORIGIN:https://yourdomain.com}
```

---

### Step 6: 프론트엔드 재빌드

```powershell
# Nginx 설정 변경 후 재빌드
docker-compose build --no-cache portal-frontend

# 재시작
docker-compose up -d portal-frontend
```

---

## 🔒 SSL 인증서 설정 (HTTPS)

### Let's Encrypt 자동 갱신

Let's Encrypt 인증서는 90일마다 갱신이 필요합니다.

**자동 갱신 스크립트 (renew-ssl.ps1):**

```powershell
# Certbot으로 인증서 갱신
certbot renew

# Nginx 재시작
docker-compose restart portal-frontend
```

**Windows 작업 스케줄러에 등록:**
- 매월 1일 실행
- 또는 Certbot의 자동 갱신 기능 사용

---

## 🌐 DNS 설정

### A 레코드 설정

도메인 제공업체의 DNS 관리 페이지에서:

```
타입: A
이름: @
값: 116.125.170.79
TTL: 3600

타입: A
이름: www
값: 116.125.170.79
TTL: 3600
```

### CNAME 레코드 (선택)

서브도메인 사용 시:

```
타입: CNAME
이름: api
값: yourdomain.com
TTL: 3600
```

---

## 📋 체크리스트

### 도메인 설정

- [ ] 도메인 구입 완료
- [ ] DNS A 레코드 설정 (도메인 → 서버 IP)
- [ ] DNS 전파 확인 (`nslookup`, `ping`)

### SSL 인증서

- [ ] SSL 인증서 발급 (Let's Encrypt 또는 상용)
- [ ] 인증서 파일 준비 (`fullchain.pem`, `privkey.pem`)
- [ ] 인증서를 서버에 복사

### Nginx 설정

- [ ] `nginx.conf`에 도메인 설정 추가
- [ ] SSL 인증서 경로 설정
- [ ] HTTP → HTTPS 리다이렉트 설정
- [ ] 프론트엔드 재빌드

### Docker 설정

- [ ] `docker-compose.yml`에 SSL 볼륨 마운트 추가
- [ ] 포트 443 추가
- [ ] 컨테이너 재시작

### CORS 설정

- [ ] `docker-compose.yml`에서 CORS_ALLOWED_ORIGIN 변경
- [ ] 백엔드 재시작

### 테스트

- [ ] `http://yourdomain.com` → `https://yourdomain.com` 리다이렉트 확인
- [ ] `https://yourdomain.com` 접속 확인
- [ ] SSL 인증서 유효성 확인 (브라우저 자물쇠 아이콘)
- [ ] API 요청 정상 동작 확인

---

## 🔄 변경 사항 요약

### 변경이 필요한 파일

1. **nginx.conf**
   - 도메인 설정
   - SSL 설정
   - HTTP → HTTPS 리다이렉트

2. **docker-compose.yml**
   - 포트 443 추가
   - SSL 볼륨 마운트
   - CORS_ALLOWED_ORIGIN 변경

3. **application-prod.yml** (선택)
   - CORS 도메인 설정

### 변경이 불필요한 부분

- ❌ 각 서비스의 소스 코드
- ❌ Dockerfile
- ❌ 데이터베이스 구조
- ❌ Docker Compose 서비스 구조

---

## 💡 실전 예제

### 예시: `jhplatform.com` 도메인 추가

#### 1. DNS 설정

```
A 레코드: jhplatform.com → 116.125.170.79
A 레코드: www.jhplatform.com → 116.125.170.79
```

#### 2. SSL 인증서 발급

```powershell
certbot certonly --standalone -d jhplatform.com -d www.jhplatform.com
```

#### 3. nginx.conf 수정

```nginx
server {
    listen 80;
    server_name jhplatform.com www.jhplatform.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name jhplatform.com www.jhplatform.com;
    
    ssl_certificate /etc/nginx/ssl/fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/privkey.pem;
    # ... 나머지 설정
}
```

#### 4. docker-compose.yml 수정

```yaml
portal-frontend:
  ports:
    - "80:80"
    - "443:443"
  volumes:
    - ./ssl:/etc/nginx/ssl:ro

auth-service:
  environment:
    - CORS_ALLOWED_ORIGIN=https://jhplatform.com
```

#### 5. 재빌드 및 재시작

```powershell
docker-compose build --no-cache portal-frontend
docker-compose up -d
docker-compose restart auth-service
```

---

## 🎯 핵심 정리

### 질문: 도메인 추가 시 많이 변경해야 하나?

**답변: 아니요, 설정 파일만 수정하면 됩니다!**

**변경 범위:**
- ✅ Nginx 설정 (도메인, SSL)
- ✅ Docker Compose (포트, 볼륨, 환경 변수)
- ✅ CORS 설정 (도메인으로 변경)

**변경 불필요:**
- ❌ 소스 코드
- ❌ 서비스 구조
- ❌ 데이터베이스

### 변경 작업량

| 작업 | 시간 | 난이도 |
|------|------|--------|
| DNS 설정 | 5분 | 쉬움 |
| SSL 인증서 발급 | 10분 | 보통 |
| Nginx 설정 | 10분 | 쉬움 |
| Docker 설정 | 5분 | 쉬움 |
| 테스트 | 10분 | 쉬움 |
| **총계** | **약 40분** | **쉬움** |

---

## 📚 추가 참고

- [Let's Encrypt 공식 문서](https://letsencrypt.org/)
- [Certbot 사용 가이드](https://certbot.eff.org/)
- [Nginx SSL 설정 가이드](https://nginx.org/en/docs/http/configuring_https_servers.html)

---

도메인 추가는 설정 파일만 수정하면 되므로 비교적 간단합니다!

