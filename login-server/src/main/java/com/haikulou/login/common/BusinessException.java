package com.haikulou.login.common;

import lombok.Getter;

import java.io.Serial;

/**
 * 业务异常。
 * <p>
 * 仅承载可预期的业务错误（如登录失败），不用于包装 NullPointerException 等
 * 运行时异常。
 *
 * @author haikulou
 */
@Getter
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ResultCode resultCode;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }
}
