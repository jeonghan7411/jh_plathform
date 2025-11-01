package jh_platform.auth.exception;

import jh_platform.auth.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리기
 * 
 * 모든 컨트롤러에서 발생하는 예외를 공통 응답 형식(ApiResponse)으로 변환합니다.
 * 
 * @RestControllerAdvice: 모든 @RestController의 예외를 처리
 * @ExceptionHandler: 특정 예외 타입을 처리하는 메서드 지정
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * BaseException 및 하위 예외를 처리
     * 
     * BaseException을 상속받은 모든 예외는 여기서 처리됩니다.
     * 예: UserNotFoundException, InvalidPasswordException 등
     * 
     * @param e 발생한 예외
     * @return 공통 응답 형식으로 변환된 에러 응답
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException e) {
        log.error("BaseException 발생: {}", e.getMessage());
        return ResponseEntity
                .status(e.getStatusCode())
                .body(ApiResponse.error(e.getStatusCode(), e.getMessage()));
    }

    /**
     * 일반적인 RuntimeException을 처리
     * 
     * 커스텀 예외가 아닌 일반 RuntimeException도 공통 응답 형식으로 변환합니다.
     * 
     * @param e 발생한 예외
     * @return 공통 응답 형식으로 변환된 에러 응답
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException e) {
        log.error("RuntimeException 발생: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "서버 오류가 발생했습니다: " + e.getMessage()));
    }

    /**
     * 모든 예외를 처리하는 최종 핸들러
     * 
     * 위에서 처리되지 않은 예외들을 여기서 처리합니다.
     * 
     * @param e 발생한 예외
     * @return 공통 응답 형식으로 변환된 에러 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("예상치 못한 예외 발생: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "예상치 못한 오류가 발생했습니다."));
    }
}

