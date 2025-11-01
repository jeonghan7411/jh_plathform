package jh_platform.auth.exception;

/**
 * 사용자명이 이미 존재할 때 발생하는 예외 (회원가입 시)
 */
public class UsernameAlreadyExistsException extends BaseException {
    
    public UsernameAlreadyExistsException() {
        super("이미 존재하는 사용자명입니다.", 409);
    }
    
    public UsernameAlreadyExistsException(String message) {
        super(message, 409);
    }
}

