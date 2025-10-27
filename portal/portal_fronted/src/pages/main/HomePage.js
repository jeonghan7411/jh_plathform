import React from "react";

const HomePage = () => {

  const token = localStorage.getItem("accessToken");
  
  return (
    <div>
      <h1>포털 메인</h1>
      {
        token ? (
          <p className="">로그인 성공 ! 토근 저장</p>
        )  : (
          <p className="">로그인 실패 ! 토근 없음</p>
        )
        
      }
    </div>
  )
};

export default HomePage;
