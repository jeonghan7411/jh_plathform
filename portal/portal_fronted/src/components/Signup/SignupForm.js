import React, { useState } from 'react';
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
  const [errorMsg, setErrorMsg] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg('');
    setSuccessMsg('');

    if (form.password !== form.confirmPassword) {
      setErrorMsg('비밀번호가 일치하지 않습니다.');
      return;
    }

    try {
      const data = await signupApi.signup({
        username: form.username,
        password: form.password,
        name: form.name,
        email: form.email,
        phone: form.phone,
      });

      if (data) {
        setSuccessMsg('회원가입이 완료되었습니다! 로그인 해주세요.');
        setTimeout(() => onSignupSuccess(), 1500);
      }
    } catch (err) {
      setErrorMsg('회원가입 중 오류가 발생했습니다. 다시 시도해주세요.');
    }
  };

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

        <button type="submit" className="signup-button">회원가입</button>

        <p className="signup-link" onClick={onSignupSuccess}>
          이미 계정이 있으신가요? 로그인하기
        </p>
      </form>
    </div>
  );
};

export default SignupForm;
