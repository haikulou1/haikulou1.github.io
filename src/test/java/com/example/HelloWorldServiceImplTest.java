package com.example;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * HelloWorldServiceImpl 单元测试
 *
 * @author DTCoder
 * @date 2025/06/20
 */
public class HelloWorldServiceImplTest {

    private final HelloWorldService helloWorldService = new HelloWorldServiceImpl();

    // ==================== greet 测试 ====================

    @Test
    public void should_returnGreeting_when_nameIsValid() {
        // Arrange
        String name = "World";

        // Act
        String result = helloWorldService.greet(name);

        // Assert
        assertNotNull("问候结果不应为null", result);
        assertEquals("Hello, World!", result);
    }

    @Test
    public void should_returnDefaultGreeting_when_nameIsNull() {
        // Arrange
        String name = null;

        // Act
        String result = helloWorldService.greet(name);

        // Assert
        assertNotNull("问候结果不应为null", result);
        assertEquals("Hello, World!", result);
    }

    @Test
    public void should_returnDefaultGreeting_when_nameIsEmpty() {
        // Arrange
        String name = "";

        // Act
        String result = helloWorldService.greet(name);

        // Assert
        assertNotNull("问候结果不应为null", result);
        assertEquals("Hello, World!", result);
    }

    @Test
    public void should_returnPersonalizedGreeting_when_nameIsProvided() {
        // Arrange
        String name = "DTCoder";

        // Act
        String result = helloWorldService.greet(name);

        // Assert
        assertNotNull("问候结果不应为null", result);
        assertEquals("Hello, DTCoder!", result);
    }
}