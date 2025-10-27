import React from 'react';
import { useNavigate } from 'react-router-dom';
import LoginForm from '../../components/login/LoginForm';

const LoginPage = () => {
  const navigate = useNavigate();

  const handleLoginSuccess = () => {
    navigate('/home');
  };

  const handleMoveSignup = () => {
    navigate('/signup');
  };

  return (
    <LoginForm
      onLoginSuccess={handleLoginSuccess}
      onMoveSignup={handleMoveSignup}
    />
  );
};

export default LoginPage;