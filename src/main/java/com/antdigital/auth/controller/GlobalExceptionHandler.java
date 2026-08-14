package com.antdigital.auth.controller;

import com.antdigital.auth.common.exception.BusinessException;
import com.antdigital.auth.common.model.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，统一封装错误响应。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常。
     *
     * @param e 业务异常
     * @return 错误响应
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        logger.warn("业务异常, code: {}, message: {}", e.getResponseCode().getCode(), e.getMessage());
        return ApiResponse.error(e.getResponseCode(), e.getUserTip());
    }

    /**
     * 处理参数校验异常。
     *
     * @param e 校验异常
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String tip = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        logger.warn("参数校验失败, tip: {}", tip);
        return ApiResponse.error(
                com.antdigital.auth.common.enums.ResponseCodeEnum.PARAM_INVALID, tip);
    }

    /**
     * 处理未知系统异常。
     *
     * @param e 异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        logger.error("系统异常, message: {}", e.getMessage(), e);
        return ApiResponse.error(
                com.antdigital.auth.common.enums.ResponseCodeEnum.SYSTEM_ERROR,
                com.antdigital.auth.common.enums.ResponseCodeEnum.SYSTEM_ERROR.getMessage());
    }
}
