package jh_platform.auth.exception;

/**
 * 비밀번호가 일치하지 않을 때 발생하는 예외
 */
public class InvalidPasswordException extends BaseException {
    
    public InvalidPasswordException() {
        super("비밀번호가 일치하지 않습니다.", 401);
    }
    
    public InvalidPasswordException(String message) {
        super(message, 401);
    }
}

