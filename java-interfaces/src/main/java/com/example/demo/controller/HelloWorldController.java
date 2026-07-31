package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * helloworld 接口。
 * <p>
 * GET /api/helloworld -> {@code {"message":"Hello World"}}
 */
@RestController
@RequestMapping("/api")
public class HelloWorldController {

    /**
     * 返回固定问候消息。
     *
     * @return 包含 {@code message} 字段的 JSON 响应体
     */
    @GetMapping("/helloworld")
    public Map<String, String> helloWorld() {
        return Map.of("message", "Hello World");
    }
}
