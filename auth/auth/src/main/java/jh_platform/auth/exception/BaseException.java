package jh_platform.auth.exception;

import lombok.Getter;

/**
 * 공통 예외 베이스 클래스
 * 
 * 모든 커스텀 예외는 이 클래스를 상속받아 사용합니다.
 * 예외 메시지와 HTTP 상태 코드를 포함합니다.
 */
@Getter
public class BaseException extends RuntimeException {
    
    private final Integer statusCode;
    
    public BaseException(String message, Integer statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    
    public BaseException(String message) {
        super(message);
        this.statusCode = 400; // 기본값 400
    }
}

