package com.haikulou.hello;

/**
 * HelloWorld 自验测试类。
 *
 * <p>仓库当前未引入 JUnit 等测试框架依赖，遵循 FIRST 原则中的
 * Self-validating（自验证）与 Independent（独立）要求，采用无依赖
 * 的 main 方法断言方式，避免引入外部依赖触发构建降级。</p>
 *
 * @author DTCoder
 * @date 2026/07/29
 */
public class HelloWorldTest {

    /**
     * 跨仓契约常量，必须与 {@link HelloWorld#getGreeting()} 返回值字面相等，
     * 用于断言前后端一致性。
     */
    private static final String EXPECTED_GREETING = "Hello, World!";

    /**
     * 测试入口，对 {@link HelloWorld#getGreeting()} 返回值进行断言。
     *
     * <p>退出码 0 表示全部断言通过，非 0 表示存在失败用例。</p>
     *
     * @param args 启动参数，本例未使用
     */
    public static void main(String[] args) {
        HelloWorld helloWorld = new HelloWorld();
        String actual = helloWorld.getGreeting();

        int failures = 0;

        // 断言1：返回值与跨仓契约常量字面相等
        if (!EXPECTED_GREETING.equals(actual)) {
            System.err.println("FAIL getGreeting returns [" + actual + "], expected [" + EXPECTED_GREETING + "]");
            failures++;
        }

        // 断言2：返回值非空，符合 NPE 防范要求
        if (actual == null) {
            System.err.println("FAIL getGreeting returns null");
            failures++;
        }

        if (failures == 0) {
            System.out.println("ALL TESTS PASSED: " + failures + " failures");
        } else {
            throw new AssertionError(failures + " test(s) failed");
        }
    }
}
