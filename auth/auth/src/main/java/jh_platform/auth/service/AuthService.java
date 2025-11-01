package jh_platform.auth.service;

import jh_platform.auth.config.JwtTokenProvider;
import jh_platform.auth.exception.InvalidPasswordException;
import jh_platform.auth.exception.UserNotFoundException;
import jh_platform.auth.exception.UsernameAlreadyExistsException;
import jh_platform.auth.mapper.UserMapper;
import jh_platform.auth.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

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
    public String login(String username, String password) {
        User user = userMapper.findByUsername(username);

        // 사용자 존재 여부 확인
        if (user == null) {
            throw new UserNotFoundException();
        }

        // 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidPasswordException();
        }

        // JWT 토큰 생성 및 반환
        return jwtTokenProvider.createToken(username);
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
