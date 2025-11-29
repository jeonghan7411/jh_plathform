import api from '../baseApi';

/**
 * 프로젝트 관련 API
 * 
 * 현재는 Mock 데이터를 반환합니다.
 * 추후 백엔드 API가 구현되면 실제 API를 호출하도록 변경합니다.
 */
export const projectApi = {
  /**
   * 프로젝트 목록 조회
   * 
   * @returns {Promise<Array>} 프로젝트 목록
   */
  getProjects: async () => {
    // TODO: 백엔드 API 구현 후 실제 API 호출로 변경
    // return await api.get('/portal/projects');
    
    // Mock 데이터 (임시)
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve([
          {
            projectCode: 'PORTAL',
            projectName: 'Portal',
            description: '전체 프로젝트를 관리하는 통합 플랫폼',
            icon: '🏠',
            isJoined: true,
            role: '관리자',
            status: 'ACTIVE',
          },
          {
            projectCode: 'CHAT',
            projectName: 'Chat',
            description: '실시간 채팅 서비스',
            icon: '💬',
            isJoined: true,
            role: '사용자',
            status: 'ACTIVE',
          },
          {
            projectCode: 'SHOP',
            projectName: 'Shop',
            description: '온라인 쇼핑몰 서비스',
            icon: '🛒',
            isJoined: false,
            status: 'AVAILABLE',
          },
          {
            projectCode: 'BLOG',
            projectName: 'Blog',
            description: '블로그 서비스',
            icon: '📝',
            isJoined: false,
            status: 'AVAILABLE',
          },
        ]);
      }, 500); // 로딩 시뮬레이션
    });
  },

  /**
   * 프로젝트 가입
   * 
   * @param {string} projectCode 프로젝트 코드
   * @returns {Promise} 가입 결과
   */
  joinProject: async (projectCode) => {
    // TODO: 백엔드 API 구현 후 실제 API 호출로 변경
    // return await api.post(`/portal/projects/${projectCode}/join`);
    
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({ success: true, message: '프로젝트에 가입되었습니다.' });
      }, 500);
    });
  },

  /**
   * 프로젝트 접근
   * 
   * @param {string} projectCode 프로젝트 코드
   * @returns {Promise} 접근 결과
   */
  accessProject: async (projectCode) => {
    // TODO: 백엔드 API 구현 후 실제 API 호출로 변경
    // return await api.get(`/portal/projects/${projectCode}/access`);
    
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({ success: true, url: `http://localhost:8081/${projectCode.toLowerCase()}` });
      }, 500);
    });
  },
};

