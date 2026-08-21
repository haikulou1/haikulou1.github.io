package com.digital.algorithm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HelloWorld 单元测试
 *
 * <p>验证 main 方法输出 "Hello, World!"。</p>
 *
 * @author DTCoder
 * @date 2025/08/18
 */
class HelloWorldTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    @DisplayName("main 方法应输出 \"Hello, World!\"")
    void should_printHelloWorld_when_mainInvoked() {
        // Act (When)
        HelloWorld.main(new String[]{});

        // Assert (Then)
        assertThat(outContent.toString(StandardCharsets.UTF_8))
                .as("HelloWorld.main 应输出 \"Hello, World!\"")
                .contains("Hello, World!");
    }
}