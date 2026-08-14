package com.antdigital.auth.common.exception;

import com.antdigital.auth.common.enums.ResponseCodeEnum;

/**
 * 业务异常，携带错误码与用户提示信息。
 */
public class BusinessException extends RuntimeException {

    private final ResponseCodeEnum responseCode;
    private final String userTip;

    public BusinessException(ResponseCodeEnum responseCode) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
        this.userTip = responseCode.getMessage();
    }

    public BusinessException(ResponseCodeEnum responseCode, String userTip) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
        this.userTip = userTip;
    }

    public ResponseCodeEnum getResponseCode() {
        return responseCode;
    }

    public String getUserTip() {
        return userTip;
    }
}
