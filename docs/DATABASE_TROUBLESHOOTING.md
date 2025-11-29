# 데이터베이스 테이블 생성 문제 해결

## 문제: 테이블이 생성되지 않음

### 원인

1. **볼륨에 이미 데이터가 있음**
   - MariaDB는 `/var/lib/mysql` 디렉토리가 비어있을 때만 초기화 스크립트 실행
   - 이전에 컨테이너를 실행했다면 볼륨에 데이터가 남아있을 수 있음

2. **한글 파일명 문제**
   - `테이블.sql` 같은 한글 파일명이 일부 환경에서 문제를 일으킬 수 있음

3. **파일 경로 문제**
   - SQL 파일이 올바른 위치에 마운트되지 않음

---

## 해결 방법

### 방법 1: 볼륨 삭제 후 재생성 (권장)

```powershell
# 1. 모든 컨테이너 중지 및 삭제
docker-compose down

# 2. 볼륨도 함께 삭제 (데이터 삭제됨!)
docker-compose down -v

# 3. 다시 시작 (테이블 자동 생성)
docker-compose up -d

# 4. 테이블 확인
docker exec -it auth-db mysql -u authuser -pauthpass authdb -e "SHOW TABLES;"
```

**주의**: 이 방법은 기존 데이터를 모두 삭제합니다!

---

### 방법 2: 수동으로 SQL 실행

```powershell
# 1. SQL 파일을 컨테이너로 복사
docker cp auth/init.sql auth-db:/tmp/init.sql

# 2. SQL 파일 실행
docker exec -it auth-db mysql -u authuser -pauthpass authdb < auth/init.sql

# 또는 컨테이너 내부에서 실행
docker exec -i auth-db mysql -u authuser -pauthpass authdb < auth/init.sql
```

---

### 방법 3: 컨테이너 내부에서 직접 실행

```powershell
# 1. 컨테이너 내부 접속
docker exec -it auth-db bash

# 2. MySQL 접속
mysql -u authuser -pauthpass authdb

# 3. SQL 파일 내용 복사하여 실행
# (init.sql 파일의 내용을 직접 입력)
```

---

## 확인 방법

### 테이블 목록 확인

```powershell
docker exec -it auth-db mysql -u authuser -pauthpass authdb -e "SHOW TABLES;"
```

**예상 결과:**
```
TB_USER
TB_ROLE
TB_USER_ROLE
TB_LOGIN_HISTORY
TB_USER_REFRESH_TOKEN
```

### 테이블 구조 확인

```powershell
# 특정 테이블 구조 확인
docker exec -it auth-db mysql -u authuser -pauthpass authdb -e "DESCRIBE TB_USER;"
```

### 초기화 스크립트 실행 여부 확인

```powershell
# 컨테이너 로그 확인
docker-compose logs auth-db | grep -i "init"
```

---

## 예방 방법

### 1. 영문 파일명 사용

`테이블.sql` → `init.sql` (영문 파일명 사용)

### 2. docker-compose.yml 설정 확인

```yaml
volumes:
  - auth-db-data:/var/lib/mysql
  - ./auth/init.sql:/docker-entrypoint-initdb.d/init.sql:ro
```

### 3. 처음 시작 시 로그 확인

```powershell
# 컨테이너 시작 시 로그 확인
docker-compose up auth-db

# 초기화 스크립트 실행 여부 확인
# "Executing /docker-entrypoint-initdb.d/init.sql" 메시지 확인
```

---

## 빠른 해결 스크립트

### PowerShell 스크립트 (reset-db.ps1)

```powershell
Write-Host "=== 데이터베이스 초기화 ===" -ForegroundColor Yellow
Write-Host "주의: 모든 데이터가 삭제됩니다!" -ForegroundColor Red

$confirm = Read-Host "계속하시겠습니까? (y/N)"
if ($confirm -ne "y") {
    Write-Host "취소되었습니다." -ForegroundColor Yellow
    exit
}

Write-Host "`n1. 컨테이너 중지 및 삭제..." -ForegroundColor Green
docker-compose down -v

Write-Host "`n2. 컨테이너 재시작..." -ForegroundColor Green
docker-compose up -d auth-db

Write-Host "`n3. 초기화 대기 (10초)..." -ForegroundColor Green
Start-Sleep -Seconds 10

Write-Host "`n4. 테이블 확인..." -ForegroundColor Green
docker exec -it auth-db mysql -u authuser -pauthpass authdb -e "SHOW TABLES;"

Write-Host "`n완료!" -ForegroundColor Green
```

---

## 문제 해결 체크리스트

- [ ] 컨테이너가 실행 중인가? (`docker-compose ps`)
- [ ] SQL 파일이 올바른 위치에 있는가? (`./auth/init.sql`)
- [ ] 볼륨에 데이터가 있는가? (`docker volume ls`)
- [ ] 컨테이너 로그에 에러가 있는가? (`docker-compose logs auth-db`)
- [ ] SQL 파일 문법이 올바른가? (MariaDB 문법 확인)

---

## 추가 정보

### MariaDB 초기화 스크립트 동작 방식

1. 컨테이너가 처음 시작될 때
2. `/var/lib/mysql` 디렉토리가 비어있으면
3. `/docker-entrypoint-initdb.d/` 디렉토리의 `.sql`, `.sh`, `.sql.gz` 파일들을
4. 알파벳 순서로 실행

**중요**: 이미 데이터가 있으면 실행되지 않습니다!

