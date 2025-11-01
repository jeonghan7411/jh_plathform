import React from 'react';
import { useNavigate } from 'react-router-dom';
import './ReturnPage.css';

const ReturnPage = () => {
  const navigate = useNavigate();

  const handleGoToLogin = () => {
    navigate('/');
  };

  const handleGoToSignup = () => {
    navigate('/signup');
  };

  return (
    <div className="return-container">
      <div className="return-card">
        <div className="return-icon">
          <svg
            width="80"
            height="80"
            viewBox="0 0 24 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"
              fill="#3b82f6"
            />
            <circle cx="12" cy="12" r="10" stroke="#3b82f6" strokeWidth="2" fill="none" opacity="0.1" />
          </svg>
        </div>
        
        <h1 className="return-title">로그인이 필요합니다</h1>
        
        <p className="return-message">
          이 페이지에 접근하려면 로그인이 필요합니다.
          <br />
          로그인하시거나 회원가입을 진행해주세요.
        </p>

        <div className="return-actions">
          <button
            onClick={handleGoToLogin}
            className="return-button return-button-primary"
          >
            로그인하기
          </button>
          
          <button
            onClick={handleGoToSignup}
            className="return-button return-button-secondary"
          >
            회원가입
          </button>
        </div>

        <div className="return-help">
          <p>
            이미 계정이 있으신가요?{' '}
            <button
              onClick={handleGoToLogin}
              className="return-link"
            >
              로그인
            </button>
            {' '}또는{' '}
            <button
              onClick={handleGoToSignup}
              className="return-link"
            >
              회원가입
            </button>
          </p>
        </div>
      </div>
    </div>
  );
};

export default ReturnPage;

