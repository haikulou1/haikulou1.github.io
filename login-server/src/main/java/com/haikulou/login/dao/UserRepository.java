package com.haikulou.login.dao;

import com.haikulou.login.entity.User;

import java.util.Optional;

/**
 * 用户仓储。
 * <p>
 * 当前以内存方式承载演示用户，后续可替换为 JPA Repository。
 *
 * @author haikulou
 */
public interface UserRepository {

    /**
     * 按用户名查找用户。
     *
     * @param username 用户名
     * @return 用户（可能为空）
     */
    Optional<User> findByUsername(String username);
}
