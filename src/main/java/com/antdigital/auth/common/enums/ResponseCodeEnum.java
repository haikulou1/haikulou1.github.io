package com.antdigital.auth.common.enums;

/**
 * 响应错误码枚举。
 *
 * <p>错误码为字符串类型，共5位：来源（A/B/C）+ 四位数字编号。
 * 来源 A 表示用户端错误，B 表示当前系统错误，C 表示第三方服务错误。</p>
 */
public enum ResponseCodeEnum {

    /** 成功 */
    SUCCESS("00000", "成功"),

    /** 参数校验失败 */
    PARAM_INVALID("A0001", "参数校验失败"),

    /** 账号或密码错误（防枚举统一提示） */
    ACCOUNT_OR_PASSWORD_ERROR("A0002", "账号或密码错误"),

    /** 账号已禁用 */
    ACCOUNT_DISABLED("A0003", "账号已禁用"),

    /** 账号已锁定 */
    ACCOUNT_LOCKED("A0004", "账号已锁定"),

    /** IP 限流 */
    IP_RATE_LIMITED("A0005", "登录尝试过于频繁"),

    /** 需要验证码 */
    CAPTCHA_REQUIRED("A0006", "需要验证码"),

    /** 验证码错误 */
    CAPTCHA_ERROR("A0007", "验证码错误"),

    /** 未登录或登录态已失效 */
    NOT_LOGIN("A0008", "未登录或登录态已失效"),

    /** 系统内部错误 */
    SYSTEM_ERROR("B0001", "系统内部错误");

    private final String code;
    private final String message;

    ResponseCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
