package com.example;

/**
 * HelloWorld 服务接口
 *
 * @author DTCoder
 * @date 2025/06/20
 */
public interface HelloWorldService {

    /**
     * 根据名称返回问候语
     *
     * @param name 名称，为null或空时返回默认问候
     * @return 问候语字符串
     */
    String greet(String name);
}