import React from "react";
import { useNavigate } from "react-router-dom";
import Header from "../../components/common/Header";
import "./AdminPage.css";

const AdminPage = () => {
  const navigate = useNavigate();

  const handleBackToDashboard = () => {
    navigate("/dashboard");
  };

  return (
    <div className="admin-container">
      <Header />
      
      <div className="admin-content">
        <div className="admin-title-section">
          <h1 className="admin-title">관리자 페이지</h1>
          <p className="admin-subtitle">시스템 관리 및 설정</p>
          <button 
            className="back-button"
            onClick={handleBackToDashboard}
          >
            ← 대시보드로 돌아가기
          </button>
        </div>

        <div className="admin-welcome">
          <p className="admin-note">관리자 페이지 기능은 추후 구현 예정입니다.</p>
        </div>

        <div className="admin-sections">
          <section className="admin-section">
            <h2>예정된 기능</h2>
            <ul className="feature-list">
              <li>사용자 관리</li>
              <li>프로젝트 관리</li>
              <li>권한 관리</li>
              <li>시스템 설정</li>
              <li>로그 조회</li>
            </ul>
          </section>
        </div>
      </div>
    </div>
  );
};

export default AdminPage;

