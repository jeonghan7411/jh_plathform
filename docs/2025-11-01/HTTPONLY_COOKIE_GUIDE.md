# HttpOnly Cookie 가이드

## 📚 HttpOnly Cookie란?

HttpOnly Cookie는 JavaScript로 접근할 수 없는 쿠키입니다. 이는 XSS(Cross-Site Scripting) 공격을 방지하기 위한 중요한 보안 기능입니다.

## 🔒 보안 비교

### ❌ localStorage 방식
```javascript
localStorage.setItem('accessToken', token);
```
- **문제점:**
  - JavaScript로 접근 가능 → XSS 공격에 취약
  - `document.cookie`보다 더 쉽게 탈취 가능
  - 브라우저 DevTools에서 확인 가능

### ✅ HttpOnly Cookie 방식
```java
Cookie cookie = new Cookie("accessToken", token);
cookie.setHttpOnly(true);  // JavaScript 접근 불가
```
- **장점:**
  - JavaScript로 접근 불가 → XSS 공격 완화
  - 브라우저가 자동으로 요청마다 전송
  - 서버가 관리하여 더 안전

## 🏗️ 구현 구조

### 백엔드 (Spring Boot)

```java
@PostMapping("/login")
public ApiResponse<Void> login(
    @RequestBody Map<String, String> request,
    HttpServletResponse response) {
    
    String token = authService.login(username, password);
    
    // HttpOnly Cookie 설정
    Cookie cookie = new Cookie("accessToken", token);
    cookie.setHttpOnly(true);     // JavaScript 접근 불가
    cookie.setSecure(false);      // 개발: false, 운영: true (HTTPS만)
    cookie.setPath("/");          // 모든 경로에서 사용 가능
    cookie.setMaxAge(3600);       // 만료 시간 (초)
    response.addCookie(cookie);
    
    return ApiResponse.success("로그인 성공");
}
```

### 프론트엔드 (React + Axios)

```javascript
// baseApi.js
const api = axios.create({
  baseURL: '/api',
  withCredentials: true,  // ⭐ 쿠키 자동 전송 필수!
});

// 요청 시 쿠키가 자동으로 전송됨
// 별도의 Authorization 헤더 불필요
```

## 🔐 쿠키 옵션 설명

### 1. HttpOnly
```java
cookie.setHttpOnly(true);
```
- **의미:** JavaScript에서 접근 불가
- **목적:** XSS 공격 방지
- **예시:** `document.cookie`로 읽을 수 없음

### 2. Secure
```java
cookie.setSecure(true);  // 운영환경
cookie.setSecure(false); // 개발환경 (HTTP)
```
- **의미:** HTTPS에서만 전송
- **목적:** 중간자 공격(MITM) 방지
- **주의:** 개발환경에서는 false, 운영환경에서는 true

### 3. SameSite
```java
// Spring Boot 3.x에서는 별도 설정 필요
// ResponseCookie 사용 또는 Filter에서 설정
```
- **옵션:**
  - `Strict`: 같은 사이트에서만 전송
  - `Lax`: GET 요청은 다른 사이트에서도 전송 가능 (기본값)
  - `None`: 모든 사이트에서 전송 (Secure 필수)

### 4. Path
```java
cookie.setPath("/");
```
- **의미:** 쿠키를 전송할 경로
- **예시:** `/api`로 설정하면 `/api/auth/login`에서만 전송

### 5. MaxAge
```java
cookie.setMaxAge(3600);  // 1시간
```
- **의미:** 쿠키 만료 시간 (초 단위)
- **주의:** 0으로 설정하면 즉시 삭제

## 📡 CORS 설정

프론트엔드와 백엔드가 다른 도메인인 경우 CORS 설정이 필요합니다:

```java
// WebConfig.java
registry.addMapping("/**")
    .allowedOrigins("http://localhost:3000")
    .allowCredentials(true);  // ⭐ 쿠키 전송을 위해 필수!
```

```javascript
// baseApi.js
withCredentials: true  // ⭐ 쿠키 수신을 위해 필수!
```

## 🔄 동작 흐름

```
1. 사용자 로그인
   ↓
2. 백엔드에서 토큰 생성
   ↓
3. HttpOnly Cookie로 토큰 저장
   Set-Cookie: accessToken=xxx; HttpOnly; Path=/; Max-Age=3600
   ↓
4. 브라우저가 쿠키 저장
   ↓
5. 이후 모든 요청에 쿠키 자동 포함
   Cookie: accessToken=xxx
   ↓
6. 백엔드에서 쿠키로 인증 확인
```

## ⚠️ 주의사항

### 1. withCredentials 필수
```javascript
// ❌ 잘못된 예
const api = axios.create({ baseURL: '/api' });

// ✅ 올바른 예
const api = axios.create({
  baseURL: '/api',
  withCredentials: true,  // 필수!
});
```

### 2. CORS 설정
```java
// ✅ CORS 설정 필수
.allowCredentials(true)
```

### 3. SameSite 설정
- CSRF 공격 방지를 위해 SameSite 설정 권장
- Spring Boot 3.x에서는 ResponseCookie 사용 또는 Filter 필요

### 4. 개발 vs 운영
```java
// 개발환경
cookie.setSecure(false);  // HTTP 허용

// 운영환경
cookie.setSecure(true);   // HTTPS만 허용
```

## 🆚 localStorage vs HttpOnly Cookie

| 항목 | localStorage | HttpOnly Cookie |
|------|--------------|-----------------|
| **JavaScript 접근** | ✅ 가능 | ❌ 불가능 |
| **XSS 공격** | ⚠️ 취약 | ✅ 안전 |
| **자동 전송** | ❌ 수동 처리 | ✅ 자동 전송 |
| **크기 제한** | ~5MB | ~4KB |
| **서버 제어** | ❌ 불가능 | ✅ 가능 |

## ✅ 권장 사항

1. **토큰은 HttpOnly Cookie에 저장**
2. **사용자 정보는 Zustand에 저장** (UI 상태)
3. **API 호출은 TanStack Query로 관리** (서버 상태)
4. **운영환경에서는 Secure 옵션 필수**
5. **SameSite 설정으로 CSRF 방지**

## 📝 참고 자료

- [MDN: Set-Cookie](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Set-Cookie)
- [OWASP: HttpOnly](https://owasp.org/www-community/HttpOnly)
- [Spring Boot: Cookies](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/cookievalue.html)

