package com.dtcoder.demo;

/**
 * HelloWorld 示例类，用于演示数科 Java 编码规范的最小可运行程序。
 *
 * @author DTCoder
 * @date 2026-07-29
 */
public class HelloWorld {

    /**
     * 输出的问候语常量，避免魔法值直接出现在代码中。
     */
    private static final String GREETING_MESSAGE = "Hello, World!";

    /**
     * 程序入口方法，向标准输出打印问候语。
     *
     * @param args 命令行参数，本示例未使用
     */
    public static void main(String[] args) {
        System.out.println(GREETING_MESSAGE);
    }
}
