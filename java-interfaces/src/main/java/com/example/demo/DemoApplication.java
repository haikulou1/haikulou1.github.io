package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 启动类。
 * <p>
 * 提供三个 REST 接口：
 * <ul>
 *     <li>helloworld  — GET  /api/helloworld</li>
 *     <li>冒泡排序    — POST /api/bubble-sort</li>
 *     <li>快速排序    — POST /api/quick-sort</li>
 * </ul>
 */
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
