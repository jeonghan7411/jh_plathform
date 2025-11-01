# 인증 시스템 문서

## 📋 목차

1. [시스템 개요](#시스템-개요)
2. [아키텍처](#아키텍처)
3. [기술 스택](#기술-스택)
4. [파일 구조](#파일-구조)
5. [로그인 흐름](#로그인-흐름)
6. [회원가입 흐름](#회원가입-흐름)
7. [예외 처리](#예외-처리)
8. [상태 관리](#상태-관리)
9. [보안](#보안)
10. [API 명세](#api-명세)

---

## 🎯 시스템 개요

이 프로젝트는 **HttpOnly Cookie + TanStack Query + Zustand**를 사용한 현대적인 인증 시스템입니다.

### 핵심 특징
- ✅ **HttpOnly Cookie**: XSS 공격 방지
- ✅ **TanStack Query**: 서버 상태 관리
- ✅ **Zustand**: 클라이언트 상태 관리
- ✅ **JWT 토큰**: 무상태 인증
- ✅ **공통 응답 형식**: 일관된 API 응답
- ✅ **전역 예외 처리**: 통합된 에러 관리

---

## 🏗️ 아키텍처

```
┌─────────────────────────────────────────────────────────┐
│                    프론트엔드 (React)                    │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  ┌──────────────┐    ┌──────────────┐                  │
│  │  Components  │───▶│   Hooks      │                  │
│  │ (LoginForm)  │    │ (useAuth)    │                  │
│  └──────────────┘    └──────┬───────┘                  │
│                              │                            │
│                    ┌─────────▼──────────┐               │
│                    │  TanStack Query    │               │
│                    │  (서버 상태)        │               │
│                    └─────────┬──────────┘               │
│                              │                            │
│                    ┌─────────▼──────────┐               │
│                    │  Zustand Store     │               │
│                    │  (클라이언트 상태)  │               │
│                    └─────────┬──────────┘               │
│                              │                            │
│                    ┌─────────▼──────────┐               │
│                    │  API Layer         │               │
│                    │  (LoginApi)        │               │
│                    └─────────┬──────────┘               │
│                              │                            │
│                    ┌─────────▼──────────┐               │
│                    │  Axios (baseApi)   │               │
│                    │  - 인터셉터        │               │
│                    │  - 쿠키 자동 전송  │               │
│                    └─────────────────────┘               │
│                                                          │
└───────────────────────┬─────────────────────────────────┘
                        │
                    HTTP Request/Response
                        │
┌───────────────────────▼─────────────────────────────────┐
│                  백엔드 (Spring Boot)                    │
├───────────────────────────────────────────────────────────┤
│                                                           │
│  ┌────────────────────────────────────┐                 │
│  │  AuthController                     │                 │
│  │  - @PostMapping("/login")           │                 │
│  │  - HttpOnly Cookie 설정             │                 │
│  └───────────────┬─────────────────────┘                 │
│                  │                                         │
│        ┌─────────▼──────────┐                            │
│        │  AuthService       │                            │
│        │  - 비즈니스 로직    │                            │
│        └─────────┬──────────┘                            │
│                  │                                         │
│        ┌─────────▼──────────┐                            │
│        │  JwtTokenProvider  │                            │
│        │  - 토큰 생성/검증  │                            │
│        └─────────┬──────────┘                            │
│                  │                                         │
│        ┌─────────▼──────────┐                            │
│        │  UserMapper        │                            │
│        │  - DB 접근         │                            │
│        └────────────────────┘                            │
│                                                           │
│  ┌────────────────────────────────────┐                 │
│  │  GlobalExceptionHandler            │                 │
│  │  - 예외 → ApiResponse 변환         │                 │
│  └────────────────────────────────────┘                 │
│                                                           │
└───────────────────────────────────────────────────────────┘
```

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
- **MariaDB**: 관계형 데이터베이스

---

## 📁 파일 구조

### 프론트엔드 구조
```
portal/portal_fronted/src/
├── api/
│   ├── baseApi.js              # Axios 인스턴스, 인터셉터
│   ├── login/
│   │   └── LoginApi.js         # 로그인 API 함수
│   └── signup/
│       └── SignupApi.js        # 회원가입 API 함수
│
├── components/
│   ├── login/
│   │   ├── LoginForm.js        # 로그인 폼 컴포넌트
│   │   └── LoginForm.css
│   └── Signup/
│       ├── SignupForm.js       # 회원가입 폼 컴포넌트
│       └── SignupForm.css
│
├── hooks/
│   └── useAuth.js              # 인증 관련 커스텀 훅
│
├── store/
│   └── authStore.js            # Zustand 인증 스토어
│
├── pages/
│   ├── login/
│   │   └── LoginPage.js
│   ├── Signup/
│   │   └── SignupPage.js
│   └── main/
│       └── HomePage.js
│
└── App.js                       # QueryClientProvider 설정
```

### 백엔드 구조
```
auth/auth/src/main/java/jh_platform/auth/
├── controller/
│   └── AuthController.java      # REST 컨트롤러
│
├── service/
│   └── AuthService.java         # 비즈니스 로직
│
├── mapper/
│   └── UserMapper.java          # MyBatis 인터페이스
│
├── model/
│   └── User.java                # 사용자 엔티티
│
├── config/
│   ├── SecurityConfig.java      # Spring Security 설정
│   ├── WebConfig.java           # CORS 설정
│   └── JwtTokenProvider.java    # JWT 토큰 생성/검증
│
├── exception/
│   ├── BaseException.java       # 예외 베이스 클래스
│   ├── UserNotFoundException.java
│   ├── InvalidPasswordException.java
│   ├── UsernameAlreadyExistsException.java
│   └── GlobalExceptionHandler.java  # 전역 예외 처리
│
└── dto/
    └── ApiResponse.java         # 공통 응답 DTO

auth/auth/src/main/resources/
├── application.yml              # 설정 파일
└── mapper/
    └── UserMapper.xml           # MyBatis SQL 매퍼
```

---

## 🔄 로그인 흐름

### 전체 흐름 다이어그램

```
1. 사용자 입력
   └─ LoginForm.js
      ├─ username, password 입력
      └─ handleSubmit() 실행

2. Mutation 트리거
   └─ useAuth.js
      ├─ loginMutation.mutate() 호출
      └─ TanStack Query 실행

3. API 호출
   └─ LoginApi.js
      └─ api.post('/auth/login', credentials)

4. Axios 요청 인터셉터
   └─ baseApi.js
      ├─ withCredentials: true 확인
      └─ 쿠키 자동 포함 (있다면)

5. HTTP 요청 전송
   POST /api/auth/login
   Headers:
     Content-Type: application/json
     Cookie: accessToken=xxx (이전 쿠키)
   Body:
     { "username": "...", "password": "..." }

6. 백엔드 수신
   └─ AuthController.java
      ├─ @PostMapping("/login") 매핑
      ├─ request에서 데이터 추출
      └─ authService.login() 호출

7. 서비스 로직
   └─ AuthService.java
      ├─ UserMapper.findByUsername() → DB 조회
      ├─ 사용자 없음? → UserNotFoundException
      ├─ PasswordEncoder.matches() → 비밀번호 검증
      ├─ 비밀번호 불일치? → InvalidPasswordException
      └─ jwtTokenProvider.createToken() → 토큰 생성

8. JWT 토큰 생성
   └─ JwtTokenProvider.java
      ├─ Claims 생성 (사용자 정보)
      ├─ 발급 시간, 만료 시간 설정
      ├─ 비밀키로 서명 (HS256)
      └─ 토큰 문자열 반환

9. HttpOnly Cookie 설정
   └─ AuthController.java
      ├─ Cookie 객체 생성
      ├─ setHttpOnly(true)
      ├─ setPath("/")
      ├─ setMaxAge(3600)
      └─ response.addCookie()

10. HTTP 응답
    Status: 200 OK
    Headers:
      Set-Cookie: accessToken=xxx; HttpOnly; Path=/; Max-Age=3600
    Body:
      {
        "success": true,
        "code": 200,
        "message": "로그인 성공",
        "data": null
      }

11. Axios 응답 인터셉터
    └─ baseApi.js
       └─ response.data 반환 (ApiResponse)

12. Mutation 성공 처리
    └─ useAuth.js
       └─ onSuccess 콜백 실행

13. 브라우저 쿠키 저장
    └─ 브라우저가 HttpOnly Cookie 자동 저장

14. 페이지 이동
    └─ LoginForm.js
       └─ onLoginSuccess() 호출
```

### 코드 단계별 설명

#### 1단계: LoginForm.js
```javascript
const { loginMutation } = useAuth();

const handleSubmit = (e) => {
  e.preventDefault();
  loginMutation.mutate(
    { username, password },
    {
      onSuccess: (response) => {
        if (response.success) {
          onLoginSuccess();  // 페이지 이동
        }
      },
    }
  );
};
```

#### 2단계: useAuth.js
```javascript
const loginMutation = useMutation({
  mutationFn: (credentials) => loginApi.login(credentials),
  onSuccess: (response) => {
    if (response.success) {
      queryClient.invalidateQueries({ queryKey: ['user'] });
    }
  },
});
```

#### 3단계: LoginApi.js
```javascript
login: async (credentials) => {
  return await api.post('/auth/login', credentials);
}
```

#### 4단계: baseApi.js
```javascript
const api = axios.create({
  withCredentials: true,  // 쿠키 자동 전송
});

api.interceptors.request.use((config) => {
  // 쿠키는 자동으로 포함됨
  return config;
});
```

#### 5-6단계: AuthController.java
```java
@PostMapping("/login")
public ApiResponse<Void> login(
    @RequestBody Map<String, String> request,
    HttpServletResponse response) {
    
    String username = request.get("username");
    String password = request.get("password");
    String token = authService.login(username, password);
    
    // HttpOnly Cookie 설정
    Cookie cookie = new Cookie("accessToken", token);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(3600);
    response.addCookie(cookie);
    
    return ApiResponse.success("로그인 성공");
}
```

#### 7단계: AuthService.java
```java
public String login(String username, String password) {
    User user = userMapper.findByUsername(username);
    if (user == null) throw new UserNotFoundException();
    if (!passwordEncoder.matches(password, user.getPassword())) {
        throw new InvalidPasswordException();
    }
    return jwtTokenProvider.createToken(username);
}
```

---

## 📝 회원가입 흐름

### 전체 흐름 (로그인과 유사하지만 차이점)

```
1. 사용자 입력
   └─ SignupForm.js
      ├─ username, password, name, email, phone 입력
      └─ handleSubmit() 실행

2. Mutation 트리거
   └─ SignupForm.js
      └─ signupMutation.mutate()

3. API 호출
   └─ SignupApi.js
      └─ api.post('/auth/signup', userData)

4. Axios 요청 인터셉터
   └─ baseApi.js (로그인과 동일)

5. HTTP 요청 전송
   POST /api/auth/signup
   Body:
     {
       "username": "...",
       "password": "...",
       "name": "...",
       "email": "...",
       "phone": "..."
     }

6. 백엔드 수신
   └─ AuthController.java
      └─ authService.signup() 호출

7. 서비스 로직
   └─ AuthService.java
      ├─ UserMapper.findByUsername() → 중복 체크
      ├─ 중복? → UsernameAlreadyExistsException
      ├─ PasswordEncoder.encode() → 비밀번호 암호화
      └─ UserMapper.insertUser() → DB 저장

8. HTTP 응답
    Status: 200 OK
    Body:
      {
        "success": true,
        "code": 200,
        "message": "회원가입이 완료되었습니다.",
        "data": null
      }

9. 성공 처리
   └─ SignupForm.js
      └─ onSignupSuccess() 호출 (로그인 페이지로 이동)
```

### 주요 차이점
- ❌ **토큰 생성 없음**: 회원가입 시 토큰 생성하지 않음
- ❌ **쿠키 설정 없음**: 로그인이 아니므로 쿠키 설정하지 않음
- ✅ **중복 체크**: 사용자명 중복 확인
- ✅ **비밀번호 암호화**: BCrypt로 암호화 후 저장

---

## ⚠️ 예외 처리

### 예외 처리 흐름

```
예외 발생 (AuthService)
    ↓
throw new UserNotFoundException()
    ↓
예외 전파 (Controller 계층으로)
    ↓
GlobalExceptionHandler 감지
    ├─ @RestControllerAdvice가 모든 컨트롤러 예외 처리
    └─ @ExceptionHandler(BaseException.class) 매칭
    ↓
handleBaseException() 실행
    ├─ e.getStatusCode() → 404 추출
    ├─ e.getMessage() → "사용자를 찾을 수 없습니다." 추출
    └─ ApiResponse.error() 생성
    ↓
HTTP 응답
{
  "success": false,
  "code": 404,
  "message": "사용자를 찾을 수 없습니다.",
  "data": null
}
    ↓
baseApi.js 응답 인터셉터
    └─ Promise.reject() 변환
    ↓
프론트엔드 catch 블록
    └─ 에러 메시지 표시
```

### 예외 클래스 계층

```
RuntimeException
    └─ BaseException (statusCode 포함)
        ├─ UserNotFoundException (404)
        ├─ InvalidPasswordException (401)
        └─ UsernameAlreadyExistsException (409)
```

### GlobalExceptionHandler.java

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException e) {
        return ResponseEntity
            .status(e.getStatusCode())
            .body(ApiResponse.error(e.getStatusCode(), e.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        return ResponseEntity
            .status(500)
            .body(ApiResponse.error(500, "예상치 못한 오류가 발생했습니다."));
    }
}
```

### 예외별 HTTP 상태 코드

| 예외 | 상태 코드 | 메시지 |
|------|-----------|--------|
| `UserNotFoundException` | 404 | "사용자를 찾을 수 없습니다." |
| `InvalidPasswordException` | 401 | "비밀번호가 일치하지 않습니다." |
| `UsernameAlreadyExistsException` | 409 | "이미 존재하는 사용자명입니다." |
| `RuntimeException` | 500 | "서버 오류가 발생했습니다." |
| `Exception` | 500 | "예상치 못한 오류가 발생했습니다." |

---

## 📊 상태 관리

### 클라이언트 상태 (Zustand)

**역할**: UI에 필요한 클라이언트 상태 관리

**저장 데이터**:
```javascript
{
  user: {
    username: "user123",
    name: "홍길동",
    email: "user@example.com"
  },
  isAuthenticated: true
}
```

**위치**: `store/authStore.js`

**사용 예시**:
```javascript
// 상태 읽기
const user = useAuthStore((state) => state.user);
const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

// 상태 변경
const setUser = useAuthStore((state) => state.setUser);
setUser(userData);

// 로그아웃
const logout = useAuthStore((state) => state.logout);
logout();
```

### 서버 상태 (TanStack Query)

**역할**: 서버에서 가져온 데이터 관리 (캐싱, 리패칭)

**저장 데이터**:
- API 응답 캐시
- 로딩 상태 (`isPending`, `isLoading`)
- 에러 상태 (`isError`, `error`)
- 성공 상태 (`isSuccess`, `data`)

**위치**: `hooks/useAuth.js`

**사용 예시**:
```javascript
// Mutation (POST, PUT, DELETE)
const loginMutation = useMutation({
  mutationFn: loginApi.login,
  onSuccess: (response) => { ... },
});

// Query (GET)
const { data, isLoading, error } = useQuery({
  queryKey: ['user'],
  queryFn: loginApi.getUserInfo,
});
```

### 상태 관리 분리 이유

| 항목 | Zustand (클라이언트) | TanStack Query (서버) |
|------|---------------------|----------------------|
| **용도** | UI 상태 | API 데이터 |
| **예시** | 로그인 여부, 사용자 정보 표시 | API 응답, 캐시 |
| **동기화** | 수동 관리 | 자동 동기화 |
| **캐싱** | ❌ 없음 | ✅ 자동 캐싱 |
| **리패칭** | ❌ 없음 | ✅ 자동 리패칭 |

---

## 🔐 보안

### HttpOnly Cookie

#### 왜 사용하는가?
- **XSS 공격 방지**: JavaScript로 토큰에 접근할 수 없음
- **자동 전송**: 브라우저가 요청마다 자동으로 포함
- **서버 관리**: 서버가 쿠키 생명주기 제어

#### 설정
```java
Cookie cookie = new Cookie("accessToken", token);
cookie.setHttpOnly(true);      // JavaScript 접근 불가
cookie.setSecure(false);        // 개발: false, 운영: true
cookie.setPath("/");            // 모든 경로
cookie.setMaxAge(3600);         // 1시간
```

#### 프론트엔드 설정
```javascript
const api = axios.create({
  withCredentials: true,  // 필수! 쿠키 전송
});
```

#### CORS 설정
```java
registry.addMapping("/**")
    .allowedOrigins("http://localhost:3000")
    .allowCredentials(true);  // 필수! 쿠키 허용
```

### JWT 토큰

#### 구조
```
Header.Payload.Signature

예시:
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
eyJzdWIiOiJ1c2VyMTIzIn0.
signature
```

#### 생성 과정
```java
Claims claims = Jwts.claims().setSubject(username);
Date now = new Date();
Date validity = new Date(now.getTime() + validityInMs);

return Jwts.builder()
    .setClaims(claims)
    .setIssuedAt(now)
    .setExpiration(validity)
    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
    .compact();
```

#### 검증
```java
Jwts.parser()
    .setSigningKey(getSigningKey())
    .parseClaimsJws(token);
```

### 비밀번호 암호화

#### BCrypt 사용
```java
// 암호화
String hashedPassword = passwordEncoder.encode(password);

// 검증
boolean matches = passwordEncoder.matches(rawPassword, hashedPassword);
```

---

## 📡 API 명세

### 공통 응답 형식

**성공 응답**:
```json
{
  "success": true,
  "code": 200,
  "message": "성공 메시지",
  "data": { ... }
}
```

**에러 응답**:
```json
{
  "success": false,
  "code": 404,
  "message": "에러 메시지",
  "data": null
}
```

### API 엔드포인트

#### 1. 회원가입
```
POST /api/auth/signup
Content-Type: application/json

Request Body:
{
  "username": "user123",
  "password": "password123",
  "name": "홍길동",
  "email": "user@example.com",
  "phone": "010-1234-5678"
}

Response:
{
  "success": true,
  "code": 200,
  "message": "회원가입이 완료되었습니다.",
  "data": null
}
```

#### 2. 로그인
```
POST /api/auth/login
Content-Type: application/json
Cookie: accessToken=xxx (이전 쿠키가 있다면)

Request Body:
{
  "username": "user123",
  "password": "password123"
}

Response Headers:
  Set-Cookie: accessToken=xxx; HttpOnly; Path=/; Max-Age=3600

Response Body:
{
  "success": true,
  "code": 200,
  "message": "로그인 성공",
  "data": null
}
```

#### 3. 로그아웃
```
POST /api/auth/logout
Cookie: accessToken=xxx

Response Headers:
  Set-Cookie: accessToken=; HttpOnly; Path=/; Max-Age=0

Response Body:
{
  "success": true,
  "code": 200,
  "message": "로그아웃 성공",
  "data": null
}
```

#### 4. 사용자 정보 조회 (예시)
```
GET /api/auth/user
Cookie: accessToken=xxx

Response:
{
  "success": true,
  "code": 200,
  "message": "Success",
  "data": {
    "username": "user123",
    "name": "홍길동",
    "email": "user@example.com"
  }
}
```

### HTTP 상태 코드

| 코드 | 의미 | 사용 시나리오 |
|------|------|--------------|
| 200 | 성공 | 로그인/회원가입 성공 |
| 401 | 인증 실패 | 비밀번호 불일치 |
| 404 | 리소스 없음 | 사용자 찾을 수 없음 |
| 409 | 충돌 | 사용자명 중복 |
| 500 | 서버 오류 | 예상치 못한 에러 |

---

## 🔧 설정 파일

### 프론트엔드: package.json
```json
{
  "dependencies": {
    "@tanstack/react-query": "^5.x.x",
    "zustand": "^4.x.x",
    "axios": "^1.12.2"
  }
}
```

### 백엔드: application.yml
```yaml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/authdb
    username: authuser
    password: authpass
  
  mybatis:
    mapper-locations: classpath:mapper/*.xml
    type-aliases-package: jh_platform.auth.model
  
  jwt:
    secret: "mySuperSecretKeyForJwtAuth1234567890"
    expiration-minutes: 60
    validation-seconds: 3600000
```

---

## 📚 참고 자료

### 공식 문서
- [TanStack Query](https://tanstack.com/query/latest)
- [Zustand](https://zustand-demo.pmnd.rs/)
- [Axios](https://axios-http.com/)
- [Spring Boot Security](https://spring.io/projects/spring-security)
- [JWT.io](https://jwt.io/)

### 보안 가이드
- [OWASP: XSS](https://owasp.org/www-community/attacks/xss/)
- [MDN: HttpOnly](https://developer.mozilla.org/en-US/docs/Web/HTTP/Cookies#restrict_access_to_cookies)

---

## 🚀 다음 단계 (확장 가능한 기능)

1. **Refresh Token**: Access Token 만료 시 자동 갱신
2. **Protected Routes**: 인증이 필요한 페이지 보호
3. **사용자 정보 API**: 로그인 후 사용자 정보 조회
4. **토큰 갱신**: 자동 토큰 갱신 로직
5. **SameSite 설정**: CSRF 공격 방지를 위한 쿠키 설정
6. **Rate Limiting**: 로그인 시도 제한

---

## 📝 변경 이력

- **2025-01-28**: 초기 구현 완료
  - HttpOnly Cookie 방식 적용
  - TanStack Query + Zustand 통합
  - 전역 예외 처리 구현

---

**문서 버전**: 1.0  
**최종 업데이트**: 2025-01-28

