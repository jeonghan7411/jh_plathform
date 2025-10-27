import React, { useState } from 'react';
import { loginApi } from '../../api/login/LoginApi';
import './LoginForm.css';


const LoginForm = ({ onLoginSuccess, onMoveSignup }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg('');

    try {
      const data = await loginApi.login({ username, password });
      if (data?.accessToken) {
        localStorage.setItem('accessToken', data.accessToken);
        onLoginSuccess();
      } else {
        setErrorMsg('로그인 실패: 토큰이 없습니다.');
      }
    } catch (err) {
      setErrorMsg('로그인 실패: 아이디 또는 비밀번호를 확인하세요.');
    }
  };

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

        <button type="submit" className="login-button">로그인</button>

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