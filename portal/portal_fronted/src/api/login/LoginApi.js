import api from '../baseApi';

export const loginApi = {
  // 로그인
  login: async (credentials) => {
    const response = await api.post('/auth/login', credentials);
    return response.data;
  },
  
  // 로그아웃
  logout: async () => {
    const response = await api.post('/auth/logout');
    return response.data;
  },
  
  // 토큰 갱신
  refreshToken: async () => {
    const response = await api.post('/auth/refresh');
    return response.data;
  },
  
  // 사용자 정보 조회
  getUserInfo: async () => {
    const response = await api.get('/auth/user');
    return response.data;
  }
};
