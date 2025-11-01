package jh_platform.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API 공통 응답 형식
 * 
 * 모든 API 응답을 일관된 형식으로 반환하기 위한 클래스입니다.
 * 
 * @param <T> 응답 데이터의 타입
 * 
 * 응답 형식:
 * {
 *   "success": true/false,
 *   "code": 200,
 *   "message": "응답 메시지",
 *   "data": { ... }
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    /**
     * 요청 성공 여부
     */
    private Boolean success;
    
    /**
     * HTTP 상태 코드
     */
    private Integer code;
    
    /**
     * 응답 메시지
     */
    private String message;
    
    /**
     * 응답 데이터 (제네릭 타입)
     */
    private T data;
    
    /**
     * 성공 응답을 생성하는 정적 팩토리 메서드
     * 
     * @param <T> 응답 데이터 타입
     * @param message 응답 메시지
     * @param data 응답 데이터
     * @return 성공 응답 객체
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(200)
                .message(message)
                .data(data)
                .build();
    }
    
    /**
     * 성공 응답을 생성하는 정적 팩토리 메서드 (메시지만)
     * 
     * @param <T> 응답 데이터 타입
     * @param message 응답 메시지
     * @return 성공 응답 객체
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(200)
                .message(message)
                .build();
    }
    
    /**
     * 성공 응답을 생성하는 정적 팩토리 메서드 (데이터만)
     * 
     * @param <T> 응답 데이터 타입
     * @param data 응답 데이터
     * @return 성공 응답 객체
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(200)
                .message("Success")
                .data(data)
                .build();
    }
    
    /**
     * 실패 응답을 생성하는 정적 팩토리 메서드
     * 
     * @param <T> 응답 데이터 타입
     * @param code HTTP 상태 코드
     * @param message 응답 메시지
     * @return 실패 응답 객체
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .build();
    }
    
    /**
     * 실패 응답을 생성하는 정적 팩토리 메서드 (기본 400 에러)
     * 
     * @param <T> 응답 데이터 타입
     * @param message 응답 메시지
     * @return 실패 응답 객체
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(400)
                .message(message)
                .build();
    }
}

