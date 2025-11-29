import React from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import LoginPage from "./pages/login/LoginPage.js";
import MainPage from "./pages/main/HomePage.js";
import SignupPage from "./pages/Signup/SignupPage.js";
import DashboardPage from "./pages/dashboard/DashboardPage.js";
import AdminPage from "./pages/admin/AdminPage.js";

// TanStack Query 클라이언트 설정
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      // 포커스 시 자동 리패치 비활성화 (필요시 true로 변경)
      refetchOnWindowFocus: false,
      // 실패 시 재시도 횟수
      retry: 1,
      // 캐시 유지 시간 (5분)
      staleTime: 5 * 60 * 1000,
    },
    mutations: {
      // mutation 실패 시 재시도 안 함
      retry: false,
    },
  },
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route path="/main" element={<DashboardPage />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/admin" element={<AdminPage />} />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  );
}

export default App;
