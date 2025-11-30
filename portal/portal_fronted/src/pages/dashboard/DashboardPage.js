import React, { useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { useAuthStore } from "../../store/authStore";
import { useQuery } from "@tanstack/react-query";
import { projectApi } from "../../api/project/ProjectApi";
import Header from "../../components/common/Header";
import Sidebar from "../../components/common/Sidebar";
import ReturnPage from "../main/ReturnPage";
import "./DashboardPage.css";

const DashboardPage = () => {
  const navigate = useNavigate();
  const hasFetchedRef = useRef(false);
  const [isSidebarCollapsed, setIsSidebarCollapsed] = React.useState(false);
  
  // Zustand에서 사용자 정보 가져오기
  const user = useAuthStore((state) => state.user);
  
  // TanStack Query 사용자 정보 조회
  const { userData, isUserLoading, refetchUser } = useAuth();

  // 컴포넌트 마운트 시 사용자 정보 확인
  useEffect(() => {
    if (hasFetchedRef.current || isUserLoading || user) {
      return;
    }
    
    hasFetchedRef.current = true;
    refetchUser().catch((error) => {
      console.log('사용자 정보 조회 실패:', error.message);
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // 프로젝트 목록 조회
  const { data: projects, isLoading, error } = useQuery({
    queryKey: ['projects'],
    queryFn: () => projectApi.getProjects(),
    staleTime: 5 * 60 * 1000, // 5분
  });

  // 프로젝트 접근
  const handleAccessProject = (projectCode) => {
    // TODO: 프로젝트별 URL로 이동
    console.log(`프로젝트 접근: ${projectCode}`);
  };

  // 인증되지 않은 경우
  if (!user && !userData && !isUserLoading) {
    return (
      <div className="dashboard-container">
        <Header />
        <div className="dashboard-content">
          <ReturnPage />
        </div>
      </div>
    );
  }

  if (isUserLoading || isLoading) {
    return (
      <div className="dashboard-container">
        <Header />
        <div className="dashboard-content">
          <div className="dashboard-loading">
            <p>로딩 중...</p>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="dashboard-container">
        <Header />
        <div className="dashboard-content">
          <div className="dashboard-error">
            <p>프로젝트 목록을 불러오는데 실패했습니다.</p>
          </div>
        </div>
      </div>
    );
  }

  // 가입된 프로젝트와 미가입 프로젝트 분리
  const joinedProjects = projects?.filter(p => p.isJoined) || [];
  const availableProjects = projects?.filter(p => !p.isJoined) || [];

  return (
    <div className="dashboard-container">
      <Header />
      <Sidebar onToggle={setIsSidebarCollapsed} />
      
      <div className={`dashboard-content ${isSidebarCollapsed ? 'sidebar-collapsed' : ''}`}>
        <div className="dashboard-title-section">
          <h1 className="dashboard-title">대시보드</h1>
          <p className="dashboard-subtitle">프로젝트를 선택하여 시작하세요</p>
        </div>

        {/* 가입된 프로젝트 */}
        <section className="projects-section">
          <h2 className="section-title">가입된 프로젝트</h2>
          {joinedProjects.length > 0 ? (
            <div className="projects-grid">
              {joinedProjects.map((project) => (
                <div key={project.projectCode} className="project-card joined">
                  <div className="project-icon">
                    {project.icon || "📦"}
                  </div>
                  <h3 className="project-name">{project.projectName}</h3>
                  <p className="project-description">{project.description}</p>
                  <div className="project-meta">
                    <span className="project-status joined">가입됨</span>
                    <span className="project-role">{project.role || "사용자"}</span>
                  </div>
                  <button
                    className="project-access-button"
                    onClick={() => handleAccessProject(project.projectCode)}
                  >
                    접근하기
                  </button>
                </div>
              ))}
            </div>
          ) : (
            <div className="empty-state">
              <p>가입된 프로젝트가 없습니다.</p>
            </div>
          )}
        </section>

        {/* 사용 가능한 프로젝트 */}
        <section className="projects-section">
          <h2 className="section-title">사용 가능한 프로젝트</h2>
          {availableProjects.length > 0 ? (
            <div className="projects-grid">
              {availableProjects.map((project) => (
                <div key={project.projectCode} className="project-card available">
                  <div className="project-icon">
                    {project.icon || "📦"}
                  </div>
                  <h3 className="project-name">{project.projectName}</h3>
                  <p className="project-description">{project.description}</p>
                  <div className="project-meta">
                    <span className="project-status available">미가입</span>
                  </div>
                  <button
                    className="project-join-button"
                    onClick={() => handleAccessProject(project.projectCode)}
                  >
                    가입하기
                  </button>
                </div>
              ))}
            </div>
          ) : (
            <div className="empty-state">
              <p>사용 가능한 프로젝트가 없습니다.</p>
            </div>
          )}
        </section>
      </div>
    </div>
  );
};

export default DashboardPage;

