package jh_platform.auth.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * JWT (JSON Web Token) 토큰을 생성하고 검증하는 클래스
 * 
 * JWT는 세션 기반 인증과 달리 서버에 세션을 저장하지 않고,
 * 클라이언트가 토큰을 가지고 있으면 인증된 사용자로 인식하는 방식입니다.
 * 
 * JWT 구조: Header.Payload.Signature (3부분으로 구성)
 * - Header: 토큰 타입과 서명 알고리즘 정보
 * - Payload: 사용자 정보 및 클레임(claims) 데이터
 * - Signature: Header와 Payload를 비밀키로 서명한 값
 */
@Component
public class JwtTokenProvider {

    /**
     * JWT 토큰 서명에 사용할 비밀키
     * application.yml에서 주입받습니다.
     * 
     * 주의: 실제 운영 환경에서는 환경변수로 관리하는 것이 안전합니다.
     * 최소 32바이트(256비트) 이상의 길이가 필요합니다.
     */
    @Value("${spring.jwt.secret}")
    private String secretKey;

    /**
     * 토큰의 유효기간 (밀리초 단위)
     * application.yml에서 주입받습니다.
     * 예: 3600000 = 1시간
     */
    @Value("${spring.jwt.validation-seconds}")
    private Long validityInMs;

    /**
     * 문자열 비밀키를 JWT 서명에 사용할 수 있는 Key 객체로 변환
     * 
     * HS256 알고리즘은 HMAC-SHA256을 사용하며,
     * 최소 256비트(32바이트) 키가 필요합니다.
     * 
     * Keys.hmacShaKeyFor()는 자동으로 키 길이를 검증하고,
     * 필요한 경우 키를 패딩하거나 해시하여 안전한 키를 생성합니다.
     * 
     * @return 서명에 사용할 Key 객체
     */
    private Key getSigningKey() {
        // 문자열 비밀키를 UTF-8 바이트 배열로 변환 후, HMAC-SHA256 키로 변환
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 사용자명을 기반으로 JWT 토큰을 생성합니다.
     * 
     * 생성 과정:
     * 1. Claims 객체 생성 및 사용자명(subject) 설정
     * 2. 현재 시간과 만료 시간 설정
     * 3. 토큰 빌더를 사용하여 토큰 구성
     * 4. 비밀키로 서명하여 최종 토큰 생성
     * 
     * @param username 토큰에 포함할 사용자명 (subject)
     * @return 생성된 JWT 토큰 문자열 (Base64 URL-safe 인코딩됨)
     */
    public String createToken(String username) {
        // Claims: JWT의 Payload 부분에 포함될 데이터들
        // setSubject: 사용자를 식별하는 고유한 값 (일반적으로 username 사용)
        Claims claims = Jwts.claims().setSubject(username);
        
        // 현재 시간
        Date now = new Date();
        
        // 만료 시간 = 현재 시간 + 유효기간
        Date validity = new Date(now.getTime() + validityInMs);

        // JWT 빌더를 사용하여 토큰 구성 및 생성
        return Jwts.builder()
                .setClaims(claims)                           // Payload에 사용자 정보 설정
                .setIssuedAt(now)                            // 토큰 발급 시간 (iat)
                .setExpiration(validity)                     // 토큰 만료 시간 (exp)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)  // 비밀키로 서명 (Header와 Payload를 암호화)
                .compact();                                  // 최종 토큰 문자열로 변환
    }

    /**
     * JWT 토큰의 유효성을 검증합니다.
     * 
     * 검증 과정:
     * 1. 토큰 파싱 시도 (형식 검증)
     * 2. 서명 검증 (비밀키로 서명된 토큰인지 확인)
     * 3. 만료 시간 검증 (exp 클레임 확인)
     * 
     * 만약 토큰이 유효하지 않다면 (만료됨, 서명이 다름, 형식 오류 등)
     * 예외가 발생하고 false를 반환합니다.
     * 
     * @param token 검증할 JWT 토큰 문자열
     * @return 토큰이 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            // 토큰 파싱 및 검증
            // parseClaimsJws(): 서명이 포함된 JWT를 파싱하며, 서명도 자동으로 검증
            Jwts.parser()
                .setSigningKey(getSigningKey())  // 서명 검증에 사용할 키 설정
                .parseClaimsJws(token);          // 토큰 파싱 및 서명 검증 수행
            
            // 위 과정에서 예외가 발생하지 않으면 토큰이 유효함
            return true;
            
        } catch (JwtException | IllegalArgumentException e) {
            // JwtException: JWT 관련 예외 (만료, 서명 불일치 등)
            // IllegalArgumentException: 잘못된 토큰 형식
            return false;
        }
    }

    /**
     * JWT 토큰에서 사용자명을 추출합니다.
     * 
     * 토큰을 파싱하여 Payload의 subject(사용자명)를 가져옵니다.
     * 이 메서드를 호출하기 전에 validateToken()으로 검증하는 것이 좋습니다.
     * 
     * @param token 사용자명을 추출할 JWT 토큰
     * @return 토큰에 저장된 사용자명 (subject)
     * @throws JwtException 토큰이 유효하지 않을 경우
     */
    public String getUsername(String token) {
        // 토큰 파싱 후 Payload(body)에서 subject 클레임 추출
        return Jwts.parser()
                .setSigningKey(getSigningKey())      // 서명 검증 키 설정
                .parseClaimsJws(token)               // 토큰 파싱
                .getBody()                           // Payload(Claims) 가져오기
                .getSubject();                       // subject 클레임 값 반환 (username)
    }
}
