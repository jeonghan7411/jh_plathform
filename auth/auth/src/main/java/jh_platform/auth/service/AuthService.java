package jh_platform.auth.service;

import jh_platform.auth.config.JwtTokenProvider;
import jh_platform.auth.exception.InvalidPasswordException;
import jh_platform.auth.exception.UserNotFoundException;
import jh_platform.auth.exception.UsernameAlreadyExistsException;
import jh_platform.auth.mapper.RefreshTokenMapper;
import jh_platform.auth.mapper.UserMapper;
import jh_platform.auth.model.LoginTokens;
import jh_platform.auth.model.RefreshToken;
import jh_platform.auth.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;

    private final RefreshTokenMapper refreshTokenMapper;

    private final JwtTokenProvider jwtTokenProvider;

    private final PasswordEncoder passwordEncoder;

    @Value("${spring.jwt.refresh-validation-millis}")
    private Long refreshValidityInMs;

    /**
     * 회원가입 서비스
     * 
     * @param user 회원가입할 사용자 정보
     * @throws UsernameAlreadyExistsException 사용자명이 이미 존재하는 경우
     */
    public void signup(User user) {
        // 사용자명 중복 체크
        User existingUser = userMapper.findByUsername(user.getUsername());
        if (existingUser != null) {
            throw new UsernameAlreadyExistsException();
        }
        
        // 비밀번호 암호화 후 저장
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insertUser(user);
    }

    /**
     * 로그인 서비스
     * 
     * @param username 사용자명
     * @param password 비밀번호
     * @return JWT 토큰
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     * @throws InvalidPasswordException 비밀번호가 일치하지 않는 경우
     */
    public LoginTokens login(String username, String password) {
        // 로그인 검증용 쿼리 사용 (비밀번호 포함)
        User user = userMapper.findByUsernameForLogin(username);

        // 사용자 존재 여부 확인
        if (user == null) {
            throw new UserNotFoundException();
        }

        // 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidPasswordException();
        }

        String accessToken = jwtTokenProvider.createAccessToken(username);
        String refreshToken = jwtTokenProvider.createRefreshToken(username);

        Instant expireInstant = Instant.now().plusMillis(refreshValidityInMs);

        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setRefreshToken(refreshToken);
        refreshTokenEntity.setUsername(username);
        refreshTokenEntity.setRevokedYn("N");
        refreshTokenEntity.setExpiresAt(LocalDateTime.ofInstant(expireInstant, ZoneId.of("Asia/Seoul")));

        refreshTokenMapper.upsertRefreshToken(refreshTokenEntity);


        LoginTokens loginTokens = new LoginTokens();
        loginTokens.setAccessToken(accessToken);
        loginTokens.setRefreshToken(refreshToken);

        // JWT 토큰 및 반환
        return loginTokens;
    }

    /**
     * Refresh Token 검증 및 Access Token 재발급
     * 
     * @param refreshToken 쿠키에서 받은 refresh token
     * @return 새로 발급된 access token
     * @throws InvalidPasswordException refresh token이 유효하지 않은 경우
     */
    public String refreshAccessToken(String refreshToken) {
        // 1. JWT 토큰 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidPasswordException(); // 또는 별도 예외 생성 가능
        }

        // 2. 토큰에서 username 추출
        String username = jwtTokenProvider.getUsername(refreshToken);

        // 3. DB에서 저장된 refresh token 조회
        RefreshToken storedToken = refreshTokenMapper.findByUsername(username);
        
        if (storedToken == null) {
            throw new InvalidPasswordException();
        }

        // 4. DB의 refresh token과 쿠키의 refresh token 일치 여부 확인
        if (!refreshToken.equals(storedToken.getRefreshToken())) {
            throw new InvalidPasswordException();
        }

        // 5. 토큰이 폐기되었는지 확인
        if ("Y".equals(storedToken.getRevokedYn())) {
            throw new InvalidPasswordException();
        }

        // 6. 토큰 만료 시간 확인 (DB의 EXPIRES_AT 기준)
        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidPasswordException();
        }

        // 7. 새 access token 발급
        return jwtTokenProvider.createAccessToken(username);
    }

    /**
     * 로그아웃 시 Refresh Token 삭제
     * 
     * @param username 사용자명
     */
    public void logout(String username) {
        log.info("로그아웃: 사용자 {}의 refreshToken 삭제 시도", username);
        try {
            refreshTokenMapper.deleteByUsername(username);
            log.info("로그아웃: 사용자 {}의 refreshToken 삭제 완료", username);
        } catch (Exception e) {
            log.error("로그아웃: 사용자 {}의 refreshToken 삭제 실패", username, e);
            throw e;
        }
    }

    /**
     * 사용자 정보 조회 서비스
     * 
     * @param username 사용자명
     * @return 사용자 정보 (비밀번호 제외)
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    public User getUserInfo(String username) {
        User user = userMapper.findByUsername(username);
        
        if (user == null) {
            throw new UserNotFoundException();
        }
        
        // 비밀번호는 제외하고 반환 (보안)
        user.setPassword(null);
        
        return user;
    }
}
