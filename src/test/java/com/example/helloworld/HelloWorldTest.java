package com.example.helloworld;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * HelloWorld 单元测试。
 * <p>
 * 通过重定向标准输出流验证 main 方法输出内容，演示数科单元测试规范：
 * 测试类以被测类名 + Test 命名，方法名表达被测行为。
 * </p>
 *
 * @author example
 * @date 2026-07-29
 */
class HelloWorldTest {

    @Test
    void mainShouldPrintHelloWorld() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(buffer));
            HelloWorld.main(new String[]{});
            assertEquals("Hello, World!" + System.lineSeparator(), buffer.toString());
        } finally {
            System.setOut(originalOut);
        }
    }
}
