package jh_platform.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jh_platform.auth.config.JwtTokenProvider;
import jh_platform.auth.dto.ApiResponse;
import jh_platform.auth.model.LoginTokens;
import jh_platform.auth.model.User;
import jh_platform.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${spring.jwt.access-validation-millis}")
    private Long accessValidityInMs;

    @Value("${spring.jwt.refresh-validation-millis}")
    private Long refreshValidityInMs;

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

        LoginTokens tokens = authService.login(username, password);

        String accessToken = tokens.getAccessToken();
        String refreshToken = tokens.getRefreshToken();

        // HttpOnly Cookie로 토큰 저장
        // Access Token 쿠키
        Cookie accessCookie = new Cookie("accessToken", accessToken);
        accessCookie.setHttpOnly(true);              // JavaScript 접근 불가 (XSS 방지)
        accessCookie.setSecure(false);               // 개발환경: false, 운영환경: true (HTTPS만)
        accessCookie.setPath("/");                   // 모든 경로에서 사용 가능
        accessCookie.setMaxAge((int) (accessValidityInMs / 1000)); // 만료 시간 (초 단위)

        // Refresh Token 쿠키
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int)(refreshValidityInMs / 1000));
        
        // SameSite 설정 (CSRF 방지)
        // Spring Boot 3.x는 HttpServletResponse에 직접 SameSite 설정이 없으므로
        // 쿠키 문자열을 직접 조작하거나 Filter에서 설정해야 함
        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        // 토큰을 응답 본문에 포함하지 않음 (보안)
        return ApiResponse.success("로그인 성공");
    }

    /**
     * Refresh Token으로 Access Token 재발급 API
     * 
     * @param request HttpServletRequest (쿠키에서 refreshToken 추출용)
     * @param response HttpServletResponse (새 accessToken 쿠키 설정용)
     * @return 성공 응답
     */
    @PostMapping("/refresh")
    public ApiResponse<Void> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {
        // 1. 쿠키에서 refreshToken 추출
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            return ApiResponse.error(401, "리프레시 토큰이 없습니다.");
        }

        try {
            // 2. Refresh Token 검증 및 새 Access Token 발급
            String newAccessToken = authService.refreshAccessToken(refreshToken);

            // 3. 새 Access Token을 쿠키로 설정
            Cookie accessCookie = new Cookie("accessToken", newAccessToken);
            accessCookie.setHttpOnly(true);
            accessCookie.setSecure(false);
            accessCookie.setPath("/");
            accessCookie.setMaxAge((int) (accessValidityInMs / 1000));
            response.addCookie(accessCookie);

            return ApiResponse.success("액세스 토큰 재발급 성공");
        } catch (Exception e) {
            return ApiResponse.error(401, "리프레시 토큰이 유효하지 않습니다.");
        }
    }

    /**
     * 로그아웃 API
     * HttpOnly Cookie 삭제 및 DB의 Refresh Token 삭제
     * 
     * @param request HttpServletRequest (쿠키에서 refreshToken 추출용)
     * @param response HttpServletResponse (쿠키 삭제용)
     * @return 성공 응답
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        // 1. 쿠키에서 refreshToken 추출하여 username 확인
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        // 2. username 추출 후 DB에서 refreshToken 삭제
        String username = null;
        
        // 방법 1: refreshToken에서 username 추출 시도
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            try {
                username = jwtTokenProvider.getUsername(refreshToken);
            } catch (Exception e) {
                // 토큰 파싱 실패 시 무시
            }
        }
        
        // 방법 2: refreshToken이 없거나 유효하지 않으면 SecurityContext에서 가져오기
        if (username == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                username = authentication.getName();
            }
        }
        
        // 3. username이 있으면 DB에서 refreshToken 삭제
        if (username != null) {
            try {
                log.info("로그아웃 요청: username={}", username);
                authService.logout(username);
            } catch (Exception e) {
                log.error("로그아웃: DB에서 refreshToken 삭제 실패, username={}", username, e);
                // DB 삭제 실패 시 로그만 남기고 계속 진행 (쿠키는 삭제)
            }
        } else {
            log.warn("로그아웃 요청: username을 찾을 수 없음 (refreshToken과 SecurityContext 모두 확인 불가)");
        }

        // 3. 쿠키 삭제 (MaxAge를 0으로 설정)
        Cookie accessCookie = new Cookie("accessToken", null);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);  // 즉시 삭제
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);  // 즉시 삭제
        response.addCookie(refreshCookie);

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
