import React, { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { signupApi } from '../../api/signup/SignupApi';
import './SignupForm.css';

const SignupForm = ({ onSignupSuccess }) => {
  const [form, setForm] = useState({
    username: '',
    password: '',
    confirmPassword: '',
    name: '',
    email: '',
    phone: ''
  });

  // TanStack Query의 useMutation 사용 (서버 상태 관리)
  const signupMutation = useMutation({
    mutationFn: (userData) => signupApi.signup(userData),
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    if (form.password !== form.confirmPassword) {
      return;
    }

    // TanStack Query Mutation 실행
    signupMutation.mutate(
      {
        username: form.username,
        password: form.password,
        name: form.name,
        email: form.email,
        phone: form.phone,
      },
      {
        onSuccess: (response) => {
          if (response.success) {
            setTimeout(() => onSignupSuccess(), 1500);
          }
        },
      }
    );
  };

  // 에러 메시지 및 성공 메시지 추출
  const errorMsg = signupMutation.isError 
    ? signupMutation.error.message 
    : form.password !== form.confirmPassword && form.confirmPassword
    ? '비밀번호가 일치하지 않습니다.'
    : null;

  const successMsg = signupMutation.isSuccess 
    ? signupMutation.data.message || '회원가입이 완료되었습니다! 로그인 해주세요.'
    : null;

  return (
    <div className="signup-container">
      <form className="signup-card" onSubmit={handleSubmit}>
        <h2 className="signup-title">회원가입</h2>

        <input
          type="text"
          name="username"
          placeholder="아이디"
          value={form.username}
          onChange={handleChange}
          className="signup-input"
          required
        />
        <input
          type="password"
          name="password"
          placeholder="비밀번호"
          value={form.password}
          onChange={handleChange}
          className="signup-input"
          required
        />
        <input
          type="password"
          name="confirmPassword"
          placeholder="비밀번호 확인"
          value={form.confirmPassword}
          onChange={handleChange}
          className="signup-input"
          required
        />
        <input
          type="text"
          name="name"
          placeholder="이름"
          value={form.name}
          onChange={handleChange}
          className="signup-input"
        />
        <input
          type="email"
          name="email"
          placeholder="이메일"
          value={form.email}
          onChange={handleChange}
          className="signup-input"
        />
        <input
          type="text"
          name="phone"
          placeholder="전화번호"
          value={form.phone}
          onChange={handleChange}
          className="signup-input"
        />

        {errorMsg && <p className="signup-message error">{errorMsg}</p>}
        {successMsg && <p className="signup-message success">{successMsg}</p>}

        <button 
          type="submit" 
          className="signup-button"
          disabled={signupMutation.isPending}
        >
          {signupMutation.isPending ? '가입 중...' : '회원가입'}
        </button>

        <p className="signup-link" onClick={onSignupSuccess}>
          이미 계정이 있으신가요? 로그인하기
        </p>
      </form>
    </div>
  );
};

export default SignupForm;
