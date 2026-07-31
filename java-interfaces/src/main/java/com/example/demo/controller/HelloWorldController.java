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

    @GetMapping("/helloworld")
    public Map<String, String> helloWorld() {
        return Map.of("message", "Hello World");
    }
}
