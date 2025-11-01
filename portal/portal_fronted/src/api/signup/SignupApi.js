import api from '../baseApi';

export const signupApi = {
  // 회원가입
  signup: async (userData) => {
    // baseApi의 인터셉터가 이미 response.data를 반환하므로
    // 여기서는 그대로 반환하면 됩니다
    return await api.post('/auth/signup', userData);
  },
  
  // 이메일 중복 확인
  checkEmail: async (email) => {
    return await api.post('/auth/check-email', { email });
  },
  
  // 아이디 중복 확인
  checkUsername: async (username) => {
    return await api.post('/auth/check-username', { username });
  },
  
  // 이메일 인증
  verifyEmail: async (token) => {
    return await api.post('/auth/verify-email', { token });
  }
};
