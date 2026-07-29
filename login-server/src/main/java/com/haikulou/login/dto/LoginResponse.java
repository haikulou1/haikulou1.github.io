package com.haikulou.login.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录响应 DTO。
 *
 * @author haikulou
 */
@Data
@AllArgsConstructor
public class LoginResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 登录成功后颁发的令牌。
     */
    private String token;
}
