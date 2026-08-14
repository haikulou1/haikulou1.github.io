package com.antdigital.auth.common.constant;

/**
 * 登录模块常量定义。
 */
public final class AuthConstants {

    private AuthConstants() {
        throw new IllegalStateException("Utility class");
    }

    /** 登录会话 Cookie 名称 */
    public static final String SESSION_COOKIE_NAME = "AUTH_SESSION";

    /** 验证码 Cookie 名称 */
    public static final String CAPTCHA_COOKIE_NAME = "AUTH_CAPTCHA";

    /** 用户状态：启用 */
    public static final int USER_STATUS_ENABLED = 1;

    /** 用户状态：禁用 */
    public static final int USER_STATUS_DISABLED = 0;

    /** Cookie SameSite 策略 */
    public static final String COOKIE_SAMESITE = "Lax";

    /** 统一防枚举提示：账号或密码错误 */
    public static final String MSG_ACCOUNT_OR_PASSWORD_ERROR = "账号或密码错误";

    /** 账号锁定提示前缀 */
    public static final String MSG_ACCOUNT_LOCKED_PREFIX = "账号已锁定，请";

    /** 账号禁用提示 */
    public static final String MSG_ACCOUNT_DISABLED = "账号已禁用，请联系管理员";

    /** IP 限流提示 */
    public static final String MSG_IP_RATE_LIMITED = "登录尝试过于频繁，请稍后再试";

    /** 需要验证码提示 */
    public static final String MSG_CAPTCHA_REQUIRED = "失败次数过多，请输入验证码";

    /** 验证码错误提示 */
    public static final String MSG_CAPTCHA_ERROR = "验证码错误";

    /** 登录成功提示 */
    public static final String MSG_LOGIN_SUCCESS = "登录成功";

    /** 退出登录成功提示 */
    public static final String MSG_LOGOUT_SUCCESS = "已退出登录";

    /** 分钟单位 */
    public static final int SECONDS_PER_MINUTE = 60;
}
