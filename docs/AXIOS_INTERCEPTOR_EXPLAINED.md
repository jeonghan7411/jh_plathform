# Axios 인터셉터 문법 설명

## 📋 목차

1. [기본 개념](#기본-개념)
2. [문법 구조](#문법-구조)
3. [각 부분 상세 설명](#각-부분-상세-설명)
4. [실행 흐름](#실행-흐름)

---

## 🎯 기본 개념

### 인터셉터란?

**인터셉터(Interceptor)**는 요청이나 응답을 가로채서 처리하는 기능입니다.

```
일반적인 흐름:
사용자 → API 요청 → 서버 → 응답 → 사용자

인터셉터 사용:
사용자 → [인터셉터] → API 요청 → 서버 → 응답 → [인터셉터] → 사용자
```

### Axios 인터셉터 종류

1. **요청 인터셉터**: 요청을 보내기 전에 처리
2. **응답 인터셉터**: 응답을 받은 후에 처리

---

## 📝 문법 구조

```javascript
api.interceptors.response.use(
  // 첫 번째 함수: 성공 시 실행
  (response) => {
    // 성공 응답 처리
    return response.data;
  },
  
  // 두 번째 함수: 에러 시 실행
  async (error) => {
    // 에러 처리
    return Promise.reject(error);
  }
);
```

### 기본 형태

```javascript
axios.interceptors.response.use(
  성공_콜백함수,
  에러_콜백함수
);
```

---

## 🔍 각 부분 상세 설명

### 1. `api.interceptors.response.use()`

**의미**: "응답 인터셉터를 등록하겠다"

- `api`: Axios 인스턴스
- `interceptors`: 인터셉터 객체
- `response`: 응답 인터셉터
- `use()`: 인터셉터를 등록하는 메서드

### 2. 첫 번째 인자: `(response) => response.data`

**의미**: "성공 응답이 오면 `response.data`만 반환"

```javascript
(response) => response.data
```

**화살표 함수 문법**:
- `(response)`: 매개변수 (응답 객체)
- `=>`: 화살표 함수
- `response.data`: 반환값

**일반 함수로 표현하면**:
```javascript
function(response) {
  return response.data;
}
```

**왜 `response.data`만 반환?**
- Axios는 기본적으로 `{ data, status, headers, ... }` 형태로 반환
- 우리는 `data`만 필요하므로 인터셉터에서 추출

### 3. 두 번째 인자: `async (error) => { ... }`

**의미**: "에러가 발생하면 비동기로 처리"

```javascript
async (error) => {
  // 비동기 처리
}
```

**`async` 키워드**:
- 이 함수가 비동기 함수임을 의미
- `await`를 사용할 수 있음

**일반 함수로 표현하면**:
```javascript
async function(error) {
  // 비동기 처리
}
```

### 4. `const originalRequest = error.config`

**의미**: "원래 요청 정보를 저장"

```javascript
const originalRequest = error.config;
```

**`error.config`란?**
- Axios 에러 객체에는 원래 요청 정보가 포함됨
- `config`에는 URL, 메서드, 헤더 등이 들어있음

**예시**:
```javascript
// 원래 요청
api.get('/api/auth/user')

// 에러 발생 시
error.config = {
  url: '/api/auth/user',
  method: 'get',
  headers: { ... },
  // ... 기타 설정
}
```

### 5. 조건문: `if (error.response?.status === 401 && ...)`

**의미**: "401 에러이고, 아직 재시도 안 했고, 로그인/회원가입이 아닐 때"

```javascript
if (
  error.response?.status === 401 &&
  !originalRequest._retry &&
  !originalRequest.url.includes('/api/auth/login') &&
  !originalRequest.url.includes('/api/auth/signup')
) {
  // refresh 로직 실행
}
```

**각 조건 설명**:

#### `error.response?.status === 401`
- **`error.response`**: 서버 응답 객체
- **`?.`**: 옵셔널 체이닝 (Optional Chaining)
  - `error.response`가 없으면 `undefined` 반환 (에러 안 남)
  - `error.response.status`가 없으면 에러 발생
- **`=== 401`**: HTTP 상태 코드가 401 (Unauthorized)

**옵셔널 체이닝 예시**:
```javascript
// 옵셔널 체이닝 없이
if (error.response && error.response.status === 401) { }

// 옵셔널 체이닝 사용
if (error.response?.status === 401) { }
```

#### `!originalRequest._retry`
- **`!`**: 논리 NOT 연산자 (반대)
- **`originalRequest._retry`**: 재시도 플래그
- **의미**: "아직 재시도하지 않았을 때"

**왜 필요한가?**
- 무한 루프 방지
- refresh가 실패해도 다시 refresh를 시도하지 않도록

#### `!originalRequest.url.includes('/api/auth/login')`
- **`includes()`**: 문자열 포함 여부 확인
- **`!`**: 포함하지 않을 때
- **의미**: "로그인 요청이 아닐 때"

**왜 필요한가?**
- 로그인 실패 시 refresh를 시도하면 안 됨
- 무한 루프 방지

### 6. `originalRequest._retry = true`

**의미**: "재시도 플래그를 설정"

```javascript
originalRequest._retry = true;
```

**동적 속성 추가**:
- JavaScript에서는 객체에 속성을 동적으로 추가 가능
- `_retry`는 우리가 만든 커스텀 플래그

### 7. `try-catch` 문

**의미**: "에러가 발생할 수 있는 코드를 안전하게 처리"

```javascript
try {
  // 에러가 발생할 수 있는 코드
  await api.post('/api/auth/refresh');
  return api(originalRequest);
} catch (refreshError) {
  // 에러 발생 시 처리
  return Promise.reject(refreshError);
}
```

**`try` 블록**:
- 정상 실행될 코드
- 에러 발생 시 `catch`로 이동

**`catch` 블록**:
- 에러 발생 시 실행
- `refreshError`: 발생한 에러 객체

### 8. `await api.post('/api/auth/refresh')`

**의미**: "refresh API를 호출하고 완료될 때까지 기다림"

```javascript
await api.post('/api/auth/refresh');
```

**`await` 키워드**:
- 비동기 작업이 완료될 때까지 기다림
- `async` 함수 안에서만 사용 가능

**동작**:
1. `/api/auth/refresh` 호출
2. 서버에서 새 Access Token 발급
3. 쿠키에 자동으로 저장됨
4. 완료될 때까지 대기

### 9. `return api(originalRequest)`

**의미**: "원래 요청을 다시 실행하고 결과 반환"

```javascript
return api(originalRequest);
```

**동작**:
1. `originalRequest`는 원래 요청 정보
2. `api()`는 Axios 인스턴스 호출
3. 새 Access Token으로 원래 요청 재시도
4. 결과를 반환

**예시**:
```javascript
// 원래 요청
api.get('/api/auth/user')  // 401 에러 발생

// refresh 후
api(originalRequest)  // 같은 요청을 다시 실행
```

### 10. `return Promise.reject(error)`

**의미**: "에러를 그대로 전달"

```javascript
return Promise.reject(error);
```

**Promise.reject()**:
- 실패한 Promise를 반환
- 호출한 쪽에서 `.catch()`로 처리 가능

**왜 사용?**
- 인터셉터에서 처리하지 못한 에러는 그대로 전달
- 컴포넌트에서 에러 처리 가능

---

## 🔄 실행 흐름

### 시나리오 1: 정상 응답

```
1. 사용자: api.get('/api/auth/user')
   ↓
2. 서버: 성공 응답 (200)
   ↓
3. 인터셉터: (response) => response.data 실행
   ↓
4. 사용자: response.data 받음 ✅
```

### 시나리오 2: 401 에러 → Refresh 성공

```
1. 사용자: api.get('/api/auth/user')
   ↓
2. 서버: 401 에러 (Access Token 만료)
   ↓
3. 인터셉터: async (error) => { ... } 실행
   ↓
4. 조건 확인: 401이고, 재시도 안 했고, 로그인/회원가입 아님 ✅
   ↓
5. originalRequest._retry = true 설정
   ↓
6. try 블록 실행
   ↓
7. await api.post('/api/auth/refresh')
   - 새 Access Token 발급
   - 쿠키에 저장
   ↓
8. return api(originalRequest)
   - 원래 요청 재시도
   - 새 Access Token으로 성공 ✅
   ↓
9. 사용자: 정상 데이터 받음 ✅
```

### 시나리오 3: 401 에러 → Refresh 실패

```
1. 사용자: api.get('/api/auth/user')
   ↓
2. 서버: 401 에러 (Access Token 만료)
   ↓
3. 인터셉터: async (error) => { ... } 실행
   ↓
4. 조건 확인: 401이고, 재시도 안 했고, 로그인/회원가입 아님 ✅
   ↓
5. originalRequest._retry = true 설정
   ↓
6. try 블록 실행
   ↓
7. await api.post('/api/auth/refresh')
   - Refresh Token도 만료 ❌
   - 에러 발생
   ↓
8. catch 블록 실행
   ↓
9. return Promise.reject(refreshError)
   - 에러를 그대로 전달
   ↓
10. 사용자: 에러 받음 ❌
```

### 시나리오 4: 다른 에러 (404, 500 등)

```
1. 사용자: api.get('/api/auth/user')
   ↓
2. 서버: 404 에러 (Not Found)
   ↓
3. 인터셉터: async (error) => { ... } 실행
   ↓
4. 조건 확인: 401이 아님 ❌
   ↓
5. if 문 건너뜀
   ↓
6. return Promise.reject(error)
   - 에러를 그대로 전달
   ↓
7. 사용자: 404 에러 받음 ❌
```

---

## 💡 핵심 개념 정리

### 1. 화살표 함수
```javascript
// 화살표 함수
(response) => response.data

// 일반 함수
function(response) {
  return response.data;
}
```

### 2. 옵셔널 체이닝
```javascript
// 옵셔널 체이닝
error.response?.status

// 일반 방식
error.response && error.response.status
```

### 3. async/await
```javascript
// async/await
async (error) => {
  await api.post('/api/auth/refresh');
}

// Promise.then()
(error) => {
  return api.post('/api/auth/refresh')
    .then(() => { ... });
}
```

### 4. 동적 속성
```javascript
// 객체에 속성 추가
originalRequest._retry = true;
```

### 5. Promise.reject()
```javascript
// 에러 전달
return Promise.reject(error);
```

---

## 🎓 연습 문제

### 문제 1: 화살표 함수 변환

다음 화살표 함수를 일반 함수로 변환하세요:

```javascript
(response) => response.data
```

**답**:
```javascript
function(response) {
  return response.data;
}
```

### 문제 2: 옵셔널 체이닝 변환

다음 옵셔널 체이닝을 일반 방식으로 변환하세요:

```javascript
error.response?.status === 401
```

**답**:
```javascript
error.response && error.response.status === 401
```

### 문제 3: async/await 변환

다음 async/await를 Promise.then()으로 변환하세요:

```javascript
async (error) => {
  await api.post('/api/auth/refresh');
  return api(originalRequest);
}
```

**답**:
```javascript
(error) => {
  return api.post('/api/auth/refresh')
    .then(() => {
      return api(originalRequest);
    });
}
```

---

## 📚 참고 자료

- [Axios 공식 문서 - Interceptors](https://axios-http.com/docs/interceptors)
- [MDN - Arrow Functions](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Functions/Arrow_functions)
- [MDN - Optional Chaining](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Optional_chaining)
- [MDN - async/await](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Statements/async_function)

---

**이 문서는 `baseApi.js`의 인터셉터 문법을 이해하기 위한 가이드입니다.**

