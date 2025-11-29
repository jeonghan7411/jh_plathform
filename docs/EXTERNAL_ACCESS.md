# 외부 접속 설정 가이드

## 현재 상황

- 컨테이너는 내부적으로 80 포트로 실행
- 외부에서는 `외부IP:8181`로 접속
- 프론트엔드가 외부 IP:8181로 API 요청

---

## 해결 방법

### 1. 프론트엔드 API 요청을 상대 경로로 변경 ✅

`baseApi.js`는 이미 `/api`로 설정되어 있어 상대 경로로 요청합니다.
Nginx 프록시를 통해 `auth-service`로 전달됩니다.

**요청 흐름:**
```
브라우저 → 외부IP:8181/api/auth/login
  ↓
Nginx (portal-frontend) → /api 요청을 auth-service:8080으로 프록시
  ↓
auth-service → /api/auth/login 처리
```

---

### 2. CORS 설정 업데이트

외부 IP:8181로 접속하는 경우, 백엔드 CORS 설정에 해당 도메인을 추가해야 합니다.

#### 방법 1: 환경 변수로 설정 (권장)

**docker-compose.yml 수정:**

```yaml
auth-service:
  environment:
    # 외부 접속 IP:포트로 변경
    - CORS_ALLOWED_ORIGIN=http://외부IP:8181
    # 예: - CORS_ALLOWED_ORIGIN=http://192.168.1.100:8181
```

**또는 .env 파일 사용:**

```env
# .env 파일 생성
CORS_ALLOWED_ORIGIN=http://외부IP:8181
```

#### 방법 2: application-prod.yml 직접 수정

```yaml
cors:
  allowed-origins:
    - http://외부IP:8181
    # 예: - http://192.168.1.100:8181
```

---

### 3. 프론트엔드 재빌드

```powershell
# 프론트엔드만 재빌드
docker-compose build --no-cache portal-frontend

# 재시작
docker-compose up -d portal-frontend
```

---

### 4. 백엔드 재시작 (CORS 설정 변경 시)

```powershell
# CORS 설정 변경 후 재시작
docker-compose restart auth-service

# 또는 전체 재시작
docker-compose restart
```

---

## 확인 방법

### 1. 네트워크 탭 확인

브라우저 개발자 도구 → Network 탭에서:
- ✅ 올바른 경우: `외부IP:8181/api/auth/login` (상대 경로)
- ❌ 잘못된 경우: `외부IP:8181/api/auth/login` (절대 경로지만 같은 도메인)

### 2. CORS 에러 확인

브라우저 콘솔에서 CORS 에러가 발생하지 않는지 확인:
```
Access to XMLHttpRequest at 'http://외부IP:8181/api/...' from origin 'http://외부IP:8181' 
has been blocked by CORS policy
```

### 3. API 요청 테스트

```powershell
# 로그인 API 테스트
curl -X POST http://외부IP:8181/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}' \
  -v
```

---

## 문제 해결

### 문제 1: CORS 에러 발생

**해결:**
1. `application-prod.yml` 또는 환경 변수에 외부 IP:8181 추가
2. `docker-compose restart auth-service`

### 문제 2: 쿠키가 전달되지 않음

**해결:**
- `baseApi.js`에서 `withCredentials: true` 설정 확인 (이미 설정됨)
- Nginx 프록시 설정 확인 (`proxy_cookie_path / /;`)

### 문제 3: API 요청이 404 에러

**해결:**
- Nginx 프록시 설정 확인 (`location /api`)
- `auth-service` 컨테이너가 실행 중인지 확인
- `docker-compose logs auth-service`로 에러 확인

---

## 최종 설정 체크리스트

- [ ] `baseApi.js`가 `/api` (상대 경로)로 설정됨
- [ ] `application-prod.yml` 또는 환경 변수에 외부 IP:8181 추가
- [ ] 프론트엔드 재빌드 완료
- [ ] 백엔드 재시작 완료
- [ ] 네트워크 탭에서 요청 URL 확인
- [ ] CORS 에러 없음 확인

---

## 빠른 설정 스크립트

### PowerShell (setup-external.ps1)

```powershell
$externalIP = Read-Host "외부 접속 IP 입력 (예: 192.168.1.100)"
$port = Read-Host "포트 입력 (예: 8181)"

$corsOrigin = "http://${externalIP}:${port}"

Write-Host "CORS 설정: $corsOrigin" -ForegroundColor Green

# docker-compose.yml에 환경 변수 추가 (수동으로 해야 함)
Write-Host "`ndocker-compose.yml의 auth-service 환경 변수에 추가:" -ForegroundColor Yellow
Write-Host "  - CORS_ALLOWED_ORIGIN=$corsOrigin" -ForegroundColor Cyan

Write-Host "`n프론트엔드 재빌드..." -ForegroundColor Green
docker-compose build --no-cache portal-frontend

Write-Host "`n서비스 재시작..." -ForegroundColor Green
docker-compose up -d

Write-Host "`n완료! $corsOrigin 으로 접속하세요." -ForegroundColor Green
```

