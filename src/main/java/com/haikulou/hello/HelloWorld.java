package com.haikulou.hello;

/**
 * HelloWorld 后端入口类，对外提供问候语契约。
 *
 * <p>跨仓契约点：{@link #getGreeting()} 恒返回 {@code "Hello, World!"}，
 * 前端仓库（leecode/hello-frontend）必须与此常量字面相等。</p>
 *
 * @author DTCoder
 * @date 2026/07/29
 */
public class HelloWorld {

    /**
     * 问候语常量，作为前后端跨仓对齐的单一事实来源。
     */
    private static final String GREETING = "Hello, World!";

    /**
     * 获取问候语。
     *
     * <p>该方法为后端对外契约方法，返回值必须与前端常量保持一致，
     * 任何变更需同步前端仓库。</p>
     *
     * @return 问候语字符串，恒为 {@code "Hello, World!"}
     */
    public String getGreeting() {
        return GREETING;
    }

    /**
     * 程序入口，打印问候语至标准输出，用于独立运行验证。
     *
     * @param args 启动参数，本例未使用
     */
    public static void main(String[] args) {
        HelloWorld helloWorld = new HelloWorld();
        System.out.println(helloWorld.getGreeting());
    }
}
