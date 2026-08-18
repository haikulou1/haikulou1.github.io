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
    public void should_returnDefaultGreeting_when_getGreeting() {
        // Arrange & Act
        String greeting = HelloWorld.getGreeting();

        // Assert
        Assert.assertEquals(EXPECTED_GREETING, greeting);
    }

    @Test
    public void should_outputGreeting_when_mainInvoked() {
        // Act: main 方法应能正常执行不抛异常
        HelloWorld.main(new String[]{});
        // Assert: 无异常即为通过
    }

    @Test
    public void should_returnSameGreeting_when_calledMultipleTimes() {
        // Arrange & Act
        String firstCall = HelloWorld.getGreeting();
        String secondCall = HelloWorld.getGreeting();

        // Assert
        Assert.assertEquals(firstCall, secondCall);
        Assert.assertEquals(EXPECTED_GREETING, firstCall);
    }

    @Test
    public void should_returnNonNullGreeting() {
        // Act
        String greeting = HelloWorld.getGreeting();

        // Assert
        Assert.assertNotNull(greeting);
    }
}
