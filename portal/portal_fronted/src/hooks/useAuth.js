import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useAuthStore } from '../store/authStore';
import { loginApi } from '../api/login/LoginApi';

/**
 * 인증 관련 커스텀 훅
 * 
 * TanStack Query (서버 상태) + Zustand (클라이언트 상태) 통합
 */
export function useAuth() {
  const queryClient = useQueryClient();
  const { setUser, logout: zustandLogout } = useAuthStore();

  // 로그인 Mutation (서버 상태)
  const loginMutation = useMutation({
    mutationFn: async (credentials) => {
      // 로그인 API 호출
    //   const loginResponse = await loginApi.login(credentials);
      
      return await loginApi.login(credentials);
    },
  });

  // 로그아웃 Mutation (서버 상태)
  const logoutMutation = useMutation({
    mutationFn: () => loginApi.logout(),
    onSuccess: () => {
      // Zustand 상태 초기화 (클라이언트 상태)
      zustandLogout();
      // 모든 쿼리 캐시 제거
      queryClient.clear();
    },
  });

  // 사용자 정보 조회 Query (서버 상태)
  // 로그인 후 자동으로 실행되거나 수동으로 호출 가능
  const { data: userData, isLoading: isUserLoading, refetch: refetchUser } = useQuery({
    queryKey: ['user'],
    queryFn: async () => {
      const response = await loginApi.getUserInfo();
      if (response.success && response.data) {
        // Zustand에 사용자 정보 저장 (클라이언트 상태)
        setUser(response.data);
        return response.data;
      }
      throw new Error(response.message || '사용자 정보를 가져올 수 없습니다.');
    },
    enabled: false, // 로그인 성공 시 수동으로 fetchQuery로 호출
    retry: false,
  });

  return {
    // Mutations
    loginMutation,
    logoutMutation,
    
    // Queries
    userData,
    isUserLoading,
    refetchUser,  // 사용자 정보 수동 갱신용
    
    // Zustand 상태
    isAuthenticated: useAuthStore((state) => state.isAuthenticated),
    user: useAuthStore((state) => state.user),
  };
}

