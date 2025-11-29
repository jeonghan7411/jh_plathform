package jh_platform.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Security 설정 Properties
 * 
 * application.yml의 security 설정을 읽어옵니다.
 * 
 * 사용 예시:
 * security:
 *   permit-all-paths:
 *     - /api/auth/signup
 *     - /api/auth/login
 */
@Component
@ConfigurationProperties(prefix = "security")   //  yml security부분 자동 바인딩
@Getter
@Setter
public class SecurityProperties {

    /**
     * 인증 없이 접근 가능한 경로 목록
     */
    private List<String> permitAllPaths = new ArrayList<>();

    /**
     * permitAll()에 사용할 경로 배열 반환
     * 
     * @return 경로 배열
     */
    public String[] getPermitAllPathsArray() {
        return permitAllPaths.toArray(new String[0]);
    }
}

