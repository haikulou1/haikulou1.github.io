package com.example;

/**
 * HelloWorld 应用程序入口
 *
 * @author DTCoder
 * @date 2025/06/20
 */
public class HelloWorldApplication {

    /**
     * 程序入口，输出问候语
     *
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        HelloWorldService service = new HelloWorldServiceImpl();
        System.out.println(service.greet("World"));
    }
}