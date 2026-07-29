package com.example.helloworld;

/**
 * HelloWorld 示例类，提供程序入口与问候语生成能力。
 *
 * @author dtcoder
 * @date 2026/07/29
 */
public class HelloWorld {

    /**
     * 默认问候语
     */
    private static final String DEFAULT_GREETING = "Hello, World!";

    /**
     * 程序入口，输出默认问候语到标准输出。
     *
     * @param args 启动参数，当前未使用
     */
    public static void main(String[] args) {
        System.out.println(getGreeting());
    }

    /**
     * 获取默认问候语。
     *
     * @return 默认问候语字符串
     */
    public static String getGreeting() {
        return DEFAULT_GREETING;
    }
}
