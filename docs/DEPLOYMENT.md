# 배포 가이드

## 📋 목차

1. [시스템 요구사항](#시스템-요구사항)
2. [Docker 설치](#docker-설치)
3. [배포 구조](#배포-구조)
4. [배포 방법](#배포-방법)
5. [환경 변수 설정](#환경-변수-설정)
6. [문제 해결](#문제-해결)

---

## 🖥️ 시스템 요구사항

### Windows 서버
- Windows Server 2016 이상
- 최소 4GB RAM
- 최소 20GB 디스크 공간

### 필수 소프트웨어
- Docker Desktop for Windows 또는 Docker Engine
- Git (선택사항)

---

## 🐳 Docker 설치

### Docker Desktop 설치 (권장)

1. [Docker Desktop for Windows](https://www.docker.com/products/docker-desktop) 다운로드
2. 설치 후 재시작
3. Docker Desktop 실행 확인

### Docker Engine 설치 (서버 환경)

```powershell
# PowerShell에서 실행
# WSL 2 백엔드 사용 (권장)
```

---

## 🏗️ 배포 구조

```
jh_platform/                    # ← 이 디렉토리 전체를 서버로 복사
├── docker-compose.yml          # 전체 서비스 오케스트레이션
├── portal/
│   └── portal_fronted/
│       ├── Dockerfile          # Nginx + React 빌드
│       ├── nginx.conf          # Nginx 설정
│       └── .dockerignore
└── auth/
    └── auth/
        ├── Dockerfile          # Spring Boot 빌드
        └── .dockerignore
```

### 컨테이너 구조

```
┌─────────────────────────────────────────┐
│  portal-frontend (Nginx)                │
│  - 포트: 80                              │
│  - React 빌드 파일 서빙                  │
└─────────────────────────────────────────┘
              │
              │ /api 요청 프록시
              ▼
┌─────────────────────────────────────────┐
│  auth-service (Spring Boot)             │
│  - 포트: 8080                            │
│  - 인증/인가 서비스                      │
└─────────────────────────────────────────┘
              │
              │ DB 연결
              ▼
┌─────────────────────────────────────────┐
│  auth-db (MariaDB)                      │
│  - 포트: 3306                            │
│  - 사용자 데이터, 권한, 토큰              │
└─────────────────────────────────────────┘
```

---

## 🚀 배포 방법

### 방법 1: 전체 디렉토리 복사 (간단)

#### 1단계: 프로젝트 디렉토리 압축

**로컬 (Mac/Linux):**
```bash
# jh_platform 디렉토리로 이동
cd /Users/bangjeonghan/Desktop/STUDY

# 압축 (node_modules 제외)
tar -czf jh_platform.tar.gz \
  --exclude='jh_platform/portal/portal_fronted/node_modules' \
  --exclude='jh_platform/.git' \
  --exclude='jh_platform/*/build' \
  jh_platform
```

**Windows 서버에서:**
```powershell
# 압축 파일을 서버로 복사 (FTP, SCP, 공유 폴더 등)
# 예: C:\deploy\jh_platform.tar.gz

# 압축 해제
# 7-Zip 또는 PowerShell 사용
Expand-Archive -Path jh_platform.tar.gz -DestinationPath C:\deploy\
```

#### 2단계: 서버에서 실행

```powershell
# 서버에서 프로젝트 디렉토리로 이동
cd C:\deploy\jh_platform

# Docker Compose로 빌드 및 실행
docker-compose up -d --build

# 로그 확인
docker-compose logs -f
```

---

### 방법 2: Git을 통한 배포 (권장)

#### 1단계: Git 저장소 설정

```bash
# 로컬에서 Git 저장소 초기화 (아직 안 했다면)
cd /Users/bangjeonghan/Desktop/STUDY/jh_platform
git init
git add .
git commit -m "Initial commit"

# 원격 저장소에 푸시 (GitHub, GitLab 등)
git remote add origin <your-repo-url>
git push -u origin main
```

#### 2단계: 서버에서 클론

```powershell
# Windows 서버에서
cd C:\deploy
git clone <your-repo-url> jh_platform
cd jh_platform

# Docker Compose로 빌드 및 실행
docker-compose up -d --build
```

#### 3단계: 업데이트 시

```powershell
# 서버에서
cd C:\deploy\jh_platform
git pull
docker-compose up -d --build
```

---

### 방법 3: 필요한 파일만 선택적 복사

다음 파일/디렉토리만 복사:

```
jh_platform/
├── docker-compose.yml          # 필수
├── portal/
│   └── portal_fronted/
│       ├── Dockerfile          # 필수
│       ├── nginx.conf          # 필수
│       ├── package.json        # 필수
│       ├── public/             # 필수
│       └── src/                # 필수
└── auth/
    └── auth/
        ├── Dockerfile          # 필수
        ├── build.gradle        # 필수
        ├── settings.gradle     # 필수
        ├── gradle/             # 필수
        ├── src/                # 필수
        └── 테이블.sql          # 필수
```

**제외할 파일/디렉토리:**
- `node_modules/` (프론트엔드 빌드 시 자동 설치)
- `.git/` (Git 사용 시)
- `build/` (백엔드 빌드 결과물)
- `*.log` (로그 파일)

---

## ⚙️ 환경 변수 설정

### docker-compose.yml 환경 변수

#### Auth 서비스
- `SPRING_PROFILES_ACTIVE`: 프로파일 (prod)
- `SPRING_DATASOURCE_URL`: DB 연결 URL
- `SPRING_DATASOURCE_USERNAME`: DB 사용자명
- `SPRING_DATASOURCE_PASSWORD`: DB 비밀번호
- `SPRING_JWT_SECRET`: JWT 비밀키
- `SPRING_JWT_ACCESS_VALIDATION_MILLIS`: Access Token 유효시간
- `SPRING_JWT_REFRESH_VALIDATION_MILLIS`: Refresh Token 유효시간

#### MariaDB
- `MYSQL_ROOT_PASSWORD`: root 비밀번호
- `MYSQL_DATABASE`: 데이터베이스 이름
- `MYSQL_USER`: 사용자명
- `MYSQL_PASSWORD`: 비밀번호

### 환경 변수 수정 방법

`docker-compose.yml` 파일을 직접 수정하거나, `.env` 파일을 사용할 수 있습니다:

```powershell
# .env 파일 생성 (docker-compose.yml과 같은 디렉토리)
# env.example 파일을 참고하여 .env 파일 생성
```

---

## 🔧 주요 명령어

### 서비스 관리

```powershell
# 서비스 시작
docker-compose up -d

# 서비스 중지
docker-compose stop

# 서비스 중지 및 컨테이너 제거
docker-compose down

# 서비스 재시작
docker-compose restart

# 특정 서비스만 재시작
docker-compose restart auth-service
```

### 로그 확인

```powershell
# 모든 서비스 로그
docker-compose logs -f

# 특정 서비스 로그
docker-compose logs -f auth-service
docker-compose logs -f portal-frontend
docker-compose logs -f auth-db
```

### 데이터베이스 접속

```powershell
# MariaDB 컨테이너 접속
docker exec -it auth-db mysql -u authuser -p authdb

# 또는 root로 접속
docker exec -it auth-db mysql -u root -p
```

### 이미지 재빌드

```powershell
# 모든 이미지 재빌드
docker-compose build --no-cache

# 특정 서비스만 재빌드
docker-compose build --no-cache portal-frontend
```

---

## 🌐 포트 매핑

| 서비스 | 컨테이너 포트 | 호스트 포트 | 접속 URL |
|--------|-------------|------------|----------|
| Portal Frontend | 80 | 80 | http://localhost |
| Auth Service | 8080 | 8080 | http://localhost:8080 |
| Auth DB | 3306 | 3306 | localhost:3306 |

**주의**: Windows 서버에서 포트 충돌이 발생할 수 있습니다. 필요시 `docker-compose.yml`에서 포트를 변경하세요.

---

## 🔒 보안 설정

### 프로덕션 환경 권장사항

1. **JWT Secret Key 변경**
   ```yaml
   SPRING_JWT_SECRET: 강력한-랜덤-문자열-최소-32바이트
   ```

2. **DB 비밀번호 변경**
   ```yaml
   MYSQL_ROOT_PASSWORD: 강력한-비밀번호
   MYSQL_PASSWORD: 강력한-비밀번호
   ```

3. **CORS 설정**
   ```yaml
   CORS_ALLOWED_ORIGIN: https://your-domain.com
   ```

4. **HTTPS 설정** (Nginx)
   - SSL 인증서 설치
   - `nginx.conf`에 HTTPS 설정 추가

---

## 🐛 문제 해결

### 포트 충돌

```powershell
# 포트 사용 중인 프로세스 확인
netstat -ano | findstr :80
netstat -ano | findstr :8080

# docker-compose.yml에서 포트 변경
ports:
  - "8081:8080"  # 호스트 포트 변경
```

### 컨테이너가 시작되지 않음

```powershell
# 로그 확인
docker-compose logs auth-service

# 컨테이너 상태 확인
docker-compose ps

# 컨테이너 재시작
docker-compose restart auth-service
```

### 데이터베이스 연결 실패

```powershell
# DB 컨테이너 상태 확인
docker-compose ps auth-db

# DB 로그 확인
docker-compose logs auth-db

# DB 컨테이너 재시작
docker-compose restart auth-db
```

### 빌드 실패

```powershell
# 캐시 없이 재빌드
docker-compose build --no-cache

# 특정 서비스만 재빌드
docker-compose build --no-cache portal-frontend
```

---

## 📦 데이터 백업

### MariaDB 데이터 백업

```powershell
# 백업
docker exec auth-db mysqldump -u authuser -pauthpass authdb > backup.sql

# 복원
docker exec -i auth-db mysql -u authuser -pauthpass authdb < backup.sql
```

### 볼륨 백업

```powershell
# 볼륨 목록 확인
docker volume ls

# 볼륨 백업 (Windows)
docker run --rm -v jh-platform_auth-db-data:/data -v ${PWD}:/backup alpine tar czf /backup/auth-db-backup.tar.gz /data
```

---

## 🔄 업데이트 방법

### 코드 변경 후 재배포

```powershell
# 1. 코드 변경 후 Git pull (또는 파일 복사)

# 2. 서비스 중지
docker-compose down

# 3. 이미지 재빌드 및 시작
docker-compose up -d --build

# 4. 로그 확인
docker-compose logs -f
```

### 특정 서비스만 업데이트

```powershell
# 프론트엔드만 재빌드
docker-compose build portal-frontend
docker-compose up -d portal-frontend

# Auth 서비스만 재빌드
docker-compose build auth-service
docker-compose up -d auth-service
```

---

## 📝 체크리스트

배포 전 확인사항:

- [ ] Docker Desktop/Engine 설치 확인
- [ ] 포트 80, 8080, 3306 사용 가능 확인
- [ ] 환경 변수 설정 (JWT Secret, DB 비밀번호 등)
- [ ] `application-prod.yml` 설정 확인
- [ ] CORS 설정 확인 (프로덕션 도메인)
- [ ] 데이터베이스 초기화 스크립트 확인 (`테이블.sql`)

---

## 🚀 빠른 시작

```powershell
# 1. 프로젝트 디렉토리로 이동
cd C:\deploy\jh_platform

# 2. 모든 서비스 빌드 및 실행
docker-compose up -d --build

# 3. 로그 확인
docker-compose logs -f

# 4. 브라우저에서 접속
# http://localhost 또는 http://서버IP
```

---

## 📞 추가 도움말

문제가 발생하면:
1. `docker-compose logs`로 로그 확인
2. `docker-compose ps`로 컨테이너 상태 확인
3. 각 서비스의 로그 파일 확인
