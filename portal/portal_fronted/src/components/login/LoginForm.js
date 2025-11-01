import React, { useState } from 'react';
import { useAuth } from '../../hooks/useAuth';
import './LoginForm.css';


const LoginForm = ({ onLoginSuccess, onMoveSignup }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  
  // TanStack Query + Zustand 통합 훅 사용
  const { loginMutation } = useAuth();

  const handleSubmit = (e) => {
    e.preventDefault();

    // TanStack Query의 useMutation 사용
    loginMutation.mutate(
      { username, password },
      {
        onSuccess: (response) => {
          if (response.success) {
            // HttpOnly Cookie는 서버에서 자동 설정됨
            // 사용자 정보는 Zustand에 저장됨
            onLoginSuccess();
          }
        },
        onError: (error) => {
          // 에러는 이미 baseApi에서 처리됨
          console.error('로그인 실패:', error.message);
        },
      }
    );
  };

  // 에러 메시지 추출
  const errorMsg = loginMutation.isError 
    ? loginMutation.error.message 
    : null;

  return (
    <div className="login-container">
      <form className="login-card" onSubmit={handleSubmit}>
        <h2 className="login-title">로그인</h2>

        <input
          type="text"
          placeholder="아이디"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          className="login-input"
          required
        />
        <input
          type="password"
          placeholder="비밀번호"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          className="login-input"
          required
        />

        {errorMsg && <p className="login-error">{errorMsg}</p>}
        {loginMutation.isPending && <p className="login-info">로그인 중...</p>}

        <button 
          type="submit" 
          className="login-button"
          disabled={loginMutation.isPending}
        >
          {loginMutation.isPending ? '로그인 중...' : '로그인'}
        </button>

        {/* ✅ 로그인 카드 내부 하단에 회원가입 링크 추가 */}
        <div className="login-link">
          <span>아직 계정이 없으신가요?</span>
          <button
            type="button"
            className="link-button"
            onClick={onMoveSignup}
          >
            회원가입
          </button>
        </div>
      </form>
    </div>
  );
};

export default LoginForm;