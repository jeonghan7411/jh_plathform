import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import LoginForm from '../../components/login/LoginForm';

const LoginPage = () => {
  const navigate = useNavigate();
  
  // Zustand에서 사용자 정보 가져오기
  const user = useAuthStore((state) => state.user);

  // 이미 로그인되어 있으면 /main으로 리다이렉트
  useEffect(() => {
    if (user) {
      navigate('/main', { replace: true });
    }
  }, [user, navigate]);

  const handleLoginSuccess = () => {
    navigate('/main');
  };

  const handleMoveSignup = () => {
    navigate('/signup');
  };

  // 사용자 정보가 있으면 리다이렉트 중이므로 아무것도 표시하지 않음
  if (user) {
    return null;
  }

  return (
    <LoginForm
      onLoginSuccess={handleLoginSuccess}
      onMoveSignup={handleMoveSignup}
    />
  );
};

export default LoginPage;