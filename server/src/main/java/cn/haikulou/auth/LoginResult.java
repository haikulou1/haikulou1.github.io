package cn.haikulou.auth;

/**
 * 登录成功响应数据，作为 {@code ApiResponse.data} 承载。
 */
public class LoginResult {

    /** JWT Token */
    private String token;

    /** Token 有效期（秒） */
    private long expiresIn;

    /** 当前用户信息 */
    private UserInfo user;

    public LoginResult() {
    }

    public LoginResult(String token, long expiresIn, UserInfo user) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }
}
