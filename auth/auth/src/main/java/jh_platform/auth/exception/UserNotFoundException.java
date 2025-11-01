package jh_platform.auth.exception;

/**
 * 사용자를 찾을 수 없을 때 발생하는 예외
 */
public class UserNotFoundException extends BaseException {
    
    public UserNotFoundException() {
        super("아이디 또는 비밀번호를 확인하세요.", 404);
    }
    
    public UserNotFoundException(String message) {
        super(message, 404);
    }
}

