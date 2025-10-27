import React from 'react';
import { useNavigate } from 'react-router-dom';
import SignupForm from '../../components/Signup/SignupForm';

const SignupPage = () => {
  const navigate = useNavigate();
  const handleSignupSuccess = () => {
    navigate('/'); // 회원가입 후 로그인 페이지로 이동
  };

  return <SignupForm onSignupSuccess={handleSignupSuccess} />;
};

export default SignupPage;

