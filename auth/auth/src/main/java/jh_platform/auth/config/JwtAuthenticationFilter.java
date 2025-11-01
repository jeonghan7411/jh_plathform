package jh_platform.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 인증 필터
 * 
 * 모든 요청에서 쿠키의 JWT 토큰을 추출하여 검증하고,
 * 유효한 토큰이 있으면 SecurityContext에 인증 정보를 설정합니다.
 * 
 * 동작 방식:
 * 1. 쿠키에서 accessToken 추출
 * 2. 토큰 유효성 검증
 * 3. 유효하면 SecurityContext에 인증 정보 설정
 * 4. 유효하지 않으면 다음 필터로 진행 (401은 SecurityConfig에서 처리)
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        // 쿠키에서 토큰 추출
        String token = extractTokenFromCookie(request);
        
        // 토큰이 있고 유효한 경우
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 토큰에서 사용자명 추출
            String username = jwtTokenProvider.getUsername(token);
            
            // SecurityContext에 인증 정보 설정
            // ROLE_USER 권한 부여 (필요시 DB에서 조회하여 권한 설정 가능)
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                    );
            
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        // 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    /**
     * HttpServletRequest에서 accessToken 쿠키를 추출
     * 
     * @param request HttpServletRequest
     * @return JWT 토큰 문자열, 없으면 null
     */
    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}

