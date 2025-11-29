package jh_platform.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * CORS 설정 Properties
 * 
 * application.yml의 cors 설정을 읽어옵니다.
 * 
 * 사용 예시:
 * cors:
 *   allowed-origins:
 *     - http://localhost:3000
 *   allowed-methods:
 *     - GET
 *     - POST
 */
@Component
@ConfigurationProperties(prefix = "cors")
@Getter
@Setter
public class CorsProperties {

    /**
     * 허용할 Origin 목록
     */
    private List<String> allowedOrigins = new ArrayList<>();

    /**
     * 허용할 HTTP 메서드 목록
     */
    private List<String> allowedMethods = new ArrayList<>();

    /**
     * 허용할 헤더 (모든 헤더 허용: "*")
     */
    private String allowedHeaders = "*";

    /**
     * 인증 정보 허용 여부
     */
    private boolean allowCredentials = true;

    /**
     * Preflight 요청 캐시 시간 (초)
     */
    private long maxAge = 3600;

    /**
     * allowedOrigins를 배열로 변환
     * 
     * @return Origin 배열
     */
    public String[] getAllowedOriginsArray() {
        return allowedOrigins.toArray(new String[0]);
    }

    /**
     * allowedMethods를 배열로 변환
     * 
     * @return HTTP 메서드 배열
     */
    public String[] getAllowedMethodsArray() {
        return allowedMethods.toArray(new String[0]);
    }
}

