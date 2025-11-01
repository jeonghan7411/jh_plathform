import { create } from 'zustand';
import { persist } from 'zustand/middleware';

/**
 * 인증 상태 관리 스토어 (Zustand)
 * 
 * 클라이언트 상태만 관리:
 * - 사용자 정보 (이름, 이메일 등 UI에 표시할 정보)
 * - 로그인 여부 (UI 상태)
 * 
 * 주의: 토큰은 HttpOnly Cookie에 저장되므로 여기서 관리하지 않음!
 */
export const useAuthStore = create(
  persist(
    (set, get) => ({
      // 상태
      user: null,              // 사용자 정보 객체
      isAuthenticated: false,  // 로그인 여부

      // 액션: 사용자 정보 설정 및 로그인 상태 변경
      setUser: (userData) => {
        set({
          user: userData,
          isAuthenticated: true,
        });
      },

      // 액션: 로그아웃 (사용자 정보 초기화)
      logout: () => {
        set({
          user: null,
          isAuthenticated: false,
        });
      },

      // 액션: 사용자 정보 업데이트
      updateUser: (userData) => {
        set((state) => ({
          user: { ...state.user, ...userData },
        }));
      },
    }),
    {
      name: 'auth-storage',  // localStorage에 저장될 키 이름
      // 민감한 정보(토큰)는 저장하지 않음
      partialize: (state) => ({
        user: state.user,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);

