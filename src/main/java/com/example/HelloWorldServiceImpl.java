package com.example;

/**
 * HelloWorld 服务实现类
 *
 * @author DTCoder
 * @date 2025/06/20
 */
public class HelloWorldServiceImpl implements HelloWorldService {

    /** 默认问候名称 */
    private static final String DEFAULT_NAME = "World";

    /** 问候语前缀 */
    private static final String GREETING_PREFIX = "Hello, ";

    /** 问候语后缀 */
    private static final String GREETING_SUFFIX = "!";

    /**
     * 根据名称返回问候语，null或空字符串时使用默认名称
     *
     * @param name 名称
     * @return 格式化的问候语
     */
    @Override
    public String greet(String name) {
        String targetName = (name == null || name.isEmpty()) ? DEFAULT_NAME : name;
        return GREETING_PREFIX + targetName + GREETING_SUFFIX;
    }
}