package jh_platform.auth.mapper;

import jh_platform.auth.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    /**
     * 사용자 정보 조회 (비밀번호 제외)
     * 일반적인 사용자 정보 조회 시 사용
     */
    User findByUsername(String username);

    /**
     * 로그인 검증용 사용자 정보 조회 (비밀번호 포함)
     * 로그인 시 비밀번호 검증을 위해서만 사용
     */
    User findByUsernameForLogin(String username);

    void insertUser(User user);
}
