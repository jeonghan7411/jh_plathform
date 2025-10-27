import api from '../baseApi';

export const signupApi = {
  // 회원가입
  signup: async (userData) => {
    const response = await api.post('/auth/signup', userData);
    return response.data;
  },
  
  // 이메일 중복 확인
  checkEmail: async (email) => {
    const response = await api.post('/auth/check-email', { email });
    return response.data;
  },
  
  // 아이디 중복 확인
  checkUsername: async (username) => {
    const response = await api.post('/auth/check-username', { username });
    return response.data;
  },
  
  // 이메일 인증
  verifyEmail: async (token) => {
    const response = await api.post('/auth/verify-email', { token });
    return response.data;
  }
};
