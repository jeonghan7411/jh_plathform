package jh_platform.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jh_platform.auth.config.JwtTokenProvider;
import jh_platform.auth.dto.ApiResponse;
import jh_platform.auth.model.User;
import jh_platform.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${spring.jwt.validation-seconds:3600000}")
    private Long jwtExpirationSeconds;

    /**
     * 회원가입 API
     * 
     * @param user 회원가입할 사용자 정보
     * @return 성공 응답
     */
    @PostMapping("/signup")
    public ApiResponse<Void> signup(@RequestBody User user) {
        authService.signup(user);
        return ApiResponse.success("회원가입이 완료되었습니다.");
    }

    /**
     * 로그인 API
     * HttpOnly Cookie 방식으로 토큰 저장
     * 
     * @param request 로그인 요청 데이터 (username, password)
     * @param response HttpServletResponse (쿠키 설정용)
     * @return 성공 응답 (토큰은 쿠키로 전송, 응답 본문에는 포함하지 않음)
     */
    @PostMapping("/login")
    public ApiResponse<Void> login(
            @RequestBody Map<String, String> request,
            HttpServletResponse response) {
        String username = request.get("username");
        String password = request.get("password");

        String token = authService.login(username, password);

        // HttpOnly Cookie로 토큰 저장
        Cookie cookie = new Cookie("accessToken", token);
        cookie.setHttpOnly(true);              // JavaScript 접근 불가 (XSS 방지)
        cookie.setSecure(false);               // 개발환경: false, 운영환경: true (HTTPS만)
        cookie.setPath("/");                   // 모든 경로에서 사용 가능
        cookie.setMaxAge((int) (jwtExpirationSeconds / 1000)); // 만료 시간 (초 단위)
        
        // SameSite 설정 (CSRF 방지)
        // Spring Boot 3.x는 HttpServletResponse에 직접 SameSite 설정이 없으므로
        // 쿠키 문자열을 직접 조작하거나 Filter에서 설정해야 함
        response.addCookie(cookie);

        // 토큰을 응답 본문에 포함하지 않음 (보안)
        return ApiResponse.success("로그인 성공");
    }

    /**
     * 로그아웃 API
     * HttpOnly Cookie 삭제
     * 
     * JWT 필터에서 이미 인증 처리를 했으므로, authenticated 사용자만 접근 가능합니다.
     * 
     * @param response HttpServletResponse (쿠키 삭제용)
     * @return 성공 응답
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletResponse response) {
        // 쿠키 삭제 (MaxAge를 0으로 설정)
        Cookie cookie = new Cookie("accessToken", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);  // 즉시 삭제
        response.addCookie(cookie);

        return ApiResponse.success("로그아웃 성공");
    }

    /**
     * 현재 로그인한 사용자 정보 조회 API
     * 
     * JWT 필터에서 이미 인증 처리를 했으므로, SecurityContext에서 사용자명을 가져옵니다.
     * 
     * @return 사용자 정보 (비밀번호 제외)
     */
    @GetMapping("/user")
    public ApiResponse<User> getUserInfo() {
        // SecurityContext에서 인증 정보 가져오기 (JWT 필터에서 설정됨)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            // 인증되지 않은 경우 (필터에서 처리되어 발생하지 않아야 하지만 안전장치)
            return ApiResponse.error(401, "인증되지 않은 사용자입니다.");
        }
        
        // 인증 정보에서 사용자명 추출 (JWT 필터에서 설정한 username)
        String username = authentication.getName();
        
        // 사용자 정보 조회 (비밀번호 제외)
        User user = authService.getUserInfo(username);
        
        return ApiResponse.success(user);
    }
}
