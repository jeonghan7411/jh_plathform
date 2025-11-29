package jh_platform.auth.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginTokens {

    private String accessToken;

    private String refreshToken;
}
