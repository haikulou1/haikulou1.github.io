package com.example.helloworld;

import org.junit.Assert;
import org.junit.Test;

/**
 * HelloWorld 单元测试。
 *
 * @author dtcoder
 * @date 2026/07/29
 */
public class HelloWorldTest {

    /**
     * 问候语预期值
     */
    private static final String EXPECTED_GREETING = "Hello, World!";

    @Test
    public void getGreeting() {
        Assert.assertEquals(EXPECTED_GREETING, HelloWorld.getGreeting());
    }
}
