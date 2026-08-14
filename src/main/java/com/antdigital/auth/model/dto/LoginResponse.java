package com.antdigital.auth.model.dto;

/**
 * 登录响应对象。
 */
public class LoginResponse {

    /** 会话令牌 */
    private String token;

    /** 用户名 */
    private String username;

    /** 会话有效期（秒） */
    private Long expiresIn;

    public LoginResponse() {
    }

    public LoginResponse(String token, String username, Long expiresIn) {
        this.token = token;
        this.username = username;
        this.expiresIn = expiresIn;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    @Override
    public String toString() {
        return "LoginResponse{username='" + username + '\''
                + ", expiresIn=" + expiresIn
                + '}';
    }
}
