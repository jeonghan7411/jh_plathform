import React from "react";
import { useNavigate } from "react-router-dom";
import { useAuthStore } from "../../store/authStore";
import Header from "../../components/common/Header";
import Sidebar from "../../components/common/Sidebar";
import "./ProfilePage.css";

const ProfilePage = () => {
  const navigate = useNavigate();
  const [isSidebarCollapsed, setIsSidebarCollapsed] = React.useState(false);
  const user = useAuthStore((state) => state.user);

  if (!user) {
    return (
      <div className="profile-container">
        <Header />
        <div className="profile-content">
          <p>사용자 정보를 불러올 수 없습니다.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="profile-container">
      <Header />
      <Sidebar onToggle={setIsSidebarCollapsed} />
      
      <div className={`profile-content ${isSidebarCollapsed ? 'sidebar-collapsed' : ''}`}>
        <div className="profile-card">
          <h1 className="profile-title">사용자 정보</h1>
          
          <div className="profile-section">
            <div className="profile-avatar-large">
              {user.name?.charAt(0) || user.username?.charAt(0) || "U"}
            </div>
            
            <div className="profile-info">
              <div className="profile-info-item">
                <label className="profile-label">사용자명</label>
                <div className="profile-value">{user.username || "-"}</div>
              </div>
              
              <div className="profile-info-item">
                <label className="profile-label">이름</label>
                <div className="profile-value">{user.name || "-"}</div>
              </div>
              
              <div className="profile-info-item">
                <label className="profile-label">이메일</label>
                <div className="profile-value">{user.email || "이메일 미입력"}</div>
              </div>
            </div>
          </div>
          
          <div className="profile-actions">
            <button 
              className="profile-button back-button"
              onClick={() => navigate("/dashboard")}
            >
              돌아가기
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProfilePage;

