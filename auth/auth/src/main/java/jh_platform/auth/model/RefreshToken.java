package jh_platform.auth.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RefreshToken {

    private String username;

    private String refreshToken;

    private LocalDateTime expiresAt;

    private String revokedYn;

    private LocalDateTime regDt;

    private LocalDateTime updDt;
}
