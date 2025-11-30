import React from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { useAuthStore } from "../../store/authStore";
import "./Header.css";

const Header = () => {
  const navigate = useNavigate();
  const user = useAuthStore((state) => state.user);
  const { userData, isUserLoading, logoutMutation } = useAuth();

  const currentUser = user || userData;

  const handleGoToAdmin = () => {
    navigate("/admin");
  };

  const handleGoToProfile = () => {
    navigate("/dashboard/profile");
  };

  const handleLogout = () => {
    logoutMutation.mutate(undefined, {
      onSuccess: () => {
        navigate("/");
      },
    });
  };

  return (
    <header className="app-header">
      <div className="header-left">
        <h1 className="header-logo">Portal</h1>
      </div>

      <div className="header-right">
        {currentUser && (
          <>
            <button 
              className="user-info-header user-info-button"
              onClick={handleGoToProfile}
            >
              <div className="user-avatar">
                {currentUser.name?.charAt(0) || currentUser.username?.charAt(0) || "U"}
              </div>
              <div className="user-details-header">
                <span className="user-name">{currentUser.name || currentUser.username}</span>
                <span className="user-email">{currentUser.email || "이메일 미입력"}</span>
              </div>
            </button>
            <button 
              className="header-button admin-button"
              onClick={handleGoToAdmin}
            >
              관리자
            </button>
            <button 
              className="header-button logout-button"
              onClick={handleLogout}
              disabled={logoutMutation.isPending}
            >
              {logoutMutation.isPending ? "로그아웃 중..." : "로그아웃"}
            </button>
          </>
        )}
      </div>
    </header>
  );
};

export default Header;

