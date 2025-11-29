package jh_platform.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(corsProperties.getAllowedOriginsArray()) // 프로필별 허용 도메인 설정 (yml에서 관리)
                .allowedMethods(corsProperties.getAllowedMethodsArray()) // 허용할 HTTP 메소드 설정 (yml에서 관리)
                .allowedHeaders(corsProperties.getAllowedHeaders()) // 모든 헤더 허용 (yml에서 관리)
                .allowCredentials(corsProperties.isAllowCredentials()) // 인증정보 허용 여부 (yml에서 관리)
                .maxAge(corsProperties.getMaxAge()); // preflight 요청의 유효시간 설정 (yml에서 관리)
    }
}
