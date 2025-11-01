import api from '../baseApi';

export const loginApi = {
  // 로그인
  login: async (credentials) => {
    // baseApi의 인터셉터가 이미 response.data를 반환하므로
    // 여기서는 그대로 반환하면 됩니다
    return await api.post('/auth/login', credentials);
  },
  
  // 로그아웃
  logout: async () => {
    return await api.post('/auth/logout');
  },
  
  // 토큰 갱신
  refreshToken: async () => {
    return await api.post('/auth/refresh');
  },
  
  // 사용자 정보 조회
  getUserInfo: async () => {
    return await api.get('/auth/user');
  }
};
