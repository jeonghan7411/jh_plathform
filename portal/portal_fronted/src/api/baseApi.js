import axios from 'axios';

const api = axios.create({
  baseURL: process.env.REACT_APP_API_BASE_URL || '/api',
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
  (response) => {
    // 성공 응답은 response.data를 반환 (ApiResponse 형식)
    return response.data;
  },
  (error) => {
    // 에러 응답 처리
    if (error.response) {
      // 백엔드에서 보낸 에러 응답 (ApiResponse 형식)
      const apiError = error.response.data;
      
      // 401 에러인 경우 (인증 실패)
      if (error.response.status === 401) {
        // HttpOnly Cookie 방식에서는 쿠키가 서버에서 관리되므로
        // 프론트엔드에서는 쿠키 삭제 불필요
        // 로그인 페이지가 아닐 때만 리다이렉트
        if (!window.location.pathname.includes('/login')) {
          window.location.href = '/login';
        }
      }
      
      // ApiResponse 형식의 에러를 그대로 반환하여 catch에서 처리할 수 있도록 함
      return Promise.reject({
        isApiError: true,
        ...apiError, // success, code, message, data를 포함
      });
    }
    
    // 네트워크 에러 등 응답이 없는 경우
    return Promise.reject({
      isApiError: false,
      success: false,
      code: 500,
      message: '네트워크 오류가 발생했습니다.',
    });
  }
);

export default api;
