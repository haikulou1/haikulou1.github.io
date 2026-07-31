package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * 全局异常处理：将客户端输入异常统一转换为 HTTP 400 响应。
 * <p>
 * 覆盖：
 * <ul>
 *     <li>{@link IllegalArgumentException} — 排序接口 arr 为 null / 超长等业务参数校验</li>
 *     <li>{@link HttpMessageNotReadableException} — 请求体 JSON 解析失败（格式错误/缺失）</li>
 * </ul>
 * 所有 4xx 异常均记录 warn 级日志，便于排障（G16）。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务参数校验异常（arr 为 null、超长等），返回 HTTP 400。
     *
     * @param ex 非法参数异常
     * @return 包含错误信息的 400 响应体
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("非法参数: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    /**
     * 处理请求体反序列化失败（JSON 格式错误/缺失），返回 HTTP 400（G4）。
     *
     * @param ex 消息不可读异常
     * @return 包含错误信息的 400 响应体
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("请求体解析失败: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("error", "Malformed request body"));
    }
}
