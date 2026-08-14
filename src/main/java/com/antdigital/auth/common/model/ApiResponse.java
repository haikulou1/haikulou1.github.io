package com.antdigital.auth.common.model;

import com.antdigital.auth.common.enums.ResponseCodeEnum;

/**
 * 统一 API 响应结构。
 *
 * <p>包含 code、message、data 三个基础字段，错误时附带 errorCode 与 userTip。</p>
 *
 * @param <T> 业务数据类型
 */
public class ApiResponse<T> {

    /** HTTP 状态码 */
    private Integer code;

    /** 错误简短信息（开发/排查用） */
    private String message;

    /** 业务数据 */
    private T data;

    /** 错误码（错误时返回） */
    private String errorCode;

    /** 用户提示信息（错误时返回） */
    private String userTip;

    public ApiResponse() {
    }

    /**
     * 构造成功响应。
     *
     * @param data 业务数据
     * @param <T>  数据类型
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage(ResponseCodeEnum.SUCCESS.getMessage());
        response.setData(data);
        return response;
    }

    /**
     * 构造成功响应（带自定义提示）。
     *
     * @param data    业务数据
     * @param message 提示信息
     * @param <T>     数据类型
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    /**
     * 构造错误响应。
     *
     * @param responseCode 错误码枚举
     * @param userTip      用户提示信息
     * @param <T>          数据类型
     * @return 错误响应
     */
    public static <T> ApiResponse<T> error(ResponseCodeEnum responseCode, String userTip) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(500);
        response.setMessage(responseCode.getMessage());
        response.setErrorCode(responseCode.getCode());
        response.setUserTip(userTip);
        response.setData(null);
        return response;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getUserTip() {
        return userTip;
    }

    public void setUserTip(String userTip) {
        this.userTip = userTip;
    }
}
