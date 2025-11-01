import React, { useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { useAuthStore } from "../../store/authStore";
import "./HomePage.css";
import ReturnPage from "./ReturnPage";

const HomePage = () => {
  const navigate = useNavigate();
  const hasFetchedRef = useRef(false); // 한 번만 실행하기 위한 ref
  
  // Zustand에서 사용자 정보 가져오기 (클라이언트 상태)
  const user = useAuthStore((state) => state.user);
  
  // TanStack Query 사용자 정보 조회 (서버 상태)
  const { userData, isUserLoading, refetchUser, logoutMutation } = useAuth();

  // 컴포넌트 마운트 시 사용자 정보 확인 (한 번만 실행)
  useEffect(() => {
    // 이미 실행했거나 로딩 중이거나 사용자 정보가 있으면 실행하지 않음
    if (hasFetchedRef.current || isUserLoading || user) {
      return;
    }
    
    // Zustand에 사용자 정보가 없으면 서버에서 가져오기
    // (쿠키가 있다면 사용자 정보를 가져올 수 있음)
    hasFetchedRef.current = true;
    refetchUser().catch((error) => {
      // 에러가 발생해도 무시 (로그인하지 않은 경우)
      // 401 에러는 baseApi에서 이미 처리됨
      console.log('사용자 정보 조회 실패:', error.message);
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // 마운트 시 한 번만 실행

  // 로그아웃 핸들러
  const handleLogout = () => {
    logoutMutation.mutate(undefined, {
      onSuccess: () => {
        navigate("/");
      },
    });
  };

  if (isUserLoading) {
    return (
      <div className="home-container">
        <p>로딩 중...</p>
      </div>
    );
  }

  return (
    <div className="home-container">
      <h1>포털 메인</h1>
      
      {(user || userData) ? (
        <div className="user-info">
          <h2>환영합니다!</h2>
          <div className="user-details">
            <p><strong>사용자명:</strong> {user?.username || userData?.username}</p>
            <p><strong>이름:</strong> {user?.name || userData?.name || "미입력"}</p>
            <p><strong>이메일:</strong> {user?.email || userData?.email || "미입력"}</p>
            <p><strong>전화번호:</strong> {user?.phone || userData?.phone || "미입력"}</p>
          </div>
          <button 
            onClick={handleLogout} 
            className="logout-button"
            disabled={logoutMutation.isPending}
          >
            {logoutMutation.isPending ? "로그아웃 중..." : "로그아웃"}
          </button>
        </div>
      ) : (
        <div className="not-logged-in">
          <ReturnPage />
        </div>
      )}
    </div>
  );
};

export default HomePage;
