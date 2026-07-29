package com.haikulou.login.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一响应码枚举。
 *
 * @author haikulou
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /** 成功 */
    SUCCESS(200, "success"),
    /** 参数校验失败 */
    PARAM_INVALID(400, "参数校验失败"),
    /** 未认证/登录失败 */
    UNAUTHORIZED(401, "用户名或密码错误"),
    /** 系统内部错误 */
    INTERNAL_ERROR(500, "系统内部错误");

    private final int code;
    private final String message;
}
