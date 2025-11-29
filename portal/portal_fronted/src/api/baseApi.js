import axios from 'axios';

// 상대 경로로 API 요청 (Nginx 프록시 사용)
// 환경 변수가 설정되어 있지 않으면 '/api' 사용 (같은 도메인)
const api = axios.create({
  baseURL: '/api',  // 항상 상대 경로 사용 (Nginx 프록시 통해 auth-service로 전달)
  headers: {
    'Content-Type': 'application/json',
  },
  // HttpOnly Cookie를 사용하므로 withCredentials 필수
  withCredentials: true,  // 쿠키를 자동으로 전송
});

// 요청 인터셉터
// HttpOnly Cookie를 사용하므로 Authorization 헤더 추가 불필요
// 쿠키가 자동으로 전송됨
api.interceptors.request.use(
  (config) => {
    // HttpOnly Cookie 방식에서는 토큰이 쿠키에 저장되므로
    // 별도의 헤더 추가 불필요
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 응답 인터셉터 - 토큰 만료 처리 및 에러 응답 처리
api.interceptors.response.use(
  (response) => response.data,
  async (error) => {
    const originalRequest = error.config;

    // 401이고, 아직 리프레시 시도 안 했고, auth 관련 엔드포인트가 아닐 때
    if (
      error.response?.status === 401 &&
      !originalRequest._retry &&
      !originalRequest.url.includes('/api/auth/login') &&
      !originalRequest.url.includes('/api/auth/signup')
    ) {
      originalRequest._retry = true;
      try {
        // 1) refresh 호출
        await api.post('/api/auth/refresh');
        // 2) 새 access 토큰이 쿠키에 들어갔으니 원래 요청 재시도
        return api(originalRequest);
      } catch (refreshError) {
        // 리프레시도 실패 → 클라이언트 상태 정리 후 로그인 페이지로
        // useAuthStore.getState().logout();
        // window.location.href = '/';
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default api;
