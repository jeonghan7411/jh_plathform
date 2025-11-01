package jh_platform.auth.model;

import lombok.Data;

@Data
public class User {

    private Long userId;

    private String username;

    private String password;

    private String name;

    private String email;

    private String phone;

    private String status;

    private String initPwYn;

    private Integer loginFailCnt;

    private String lastLoginDt;

    private String lastUpdatePass;

    private String regDt;

    private String updDt;
}
