# JH Platform

통합 플랫폼 프로젝트 - Portal을 중심으로 여러 프로젝트를 연결하고 관리하는 시스템

## 📋 프로젝트 구조

```
jh_platform/
├── auth/                    # 인증 서비스 (Spring Boot)
│   └── auth/
│       ├── src/
│       └── Dockerfile
├── portal/                  # 포털 서비스
│   ├── portal_backend/      # 포털 백엔드 (예정)
│   └── portal_fronted/      # 포털 프론트엔드 (React)
│       └── Dockerfile
├── docker-compose.yml        # Docker Compose 설정
└── docs/                    # 문서
    ├── ARCHITECTURE.md      # 아키텍처 문서
    └── DEPLOYMENT.md        # 배포 가이드
```

## 🚀 빠른 시작

### 로컬 개발 환경

#### 프론트엔드
```bash
cd portal/portal_fronted
npm install
npm start
```

#### 백엔드
```bash
cd auth/auth
./gradlew bootRun
```

### Docker 배포

```bash
# 모든 서비스 빌드 및 실행
docker-compose up -d --build

# 로그 확인
docker-compose logs -f
```

자세한 배포 방법은 [DEPLOYMENT.md](./docs/DEPLOYMENT.md)를 참고하세요.

## 📚 문서

### 메인 가이드
- **[마스터 가이드](./docs/MASTER_GUIDE.md)** ⭐ - 모든 내용을 종합한 메인 문서

### 상세 문서
- [아키텍처 문서](./docs/ARCHITECTURE.md) - 시스템 구조 및 설계
- [배포 가이드](./docs/DEPLOYMENT_GUIDE.md) - Docker 배포 완전 가이드
- [새 프로젝트 추가](./docs/ADDING_NEW_PROJECT.md) - 새 프로젝트 추가 방법
- [도메인 설정](./docs/DOMAIN_SETUP.md) - 도메인 및 SSL 설정
- [외부 접속 설정](./docs/EXTERNAL_ACCESS.md) - 외부 IP 접속 설정
- [데이터베이스 문제 해결](./docs/DATABASE_TROUBLESHOOTING.md) - DB 관련 문제 해결

## 🛠️ 기술 스택

### 프론트엔드
- React 19.2.0
- TanStack Query
- Zustand
- Axios

### 백엔드
- Spring Boot 3.5.7
- MyBatis
- Spring Security
- JWT

### 데이터베이스
- MariaDB

### 인프라
- Docker
- Docker Compose
- Nginx

## 📝 라이선스

이 프로젝트는 학습 목적으로 제작되었습니다.

