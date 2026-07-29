package com.haikulou.login.dao;

import com.haikulou.login.entity.User;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存用户仓储实现。
 * <p>
 * 演示用途：内置一个示例用户 admin/123456。
 *
 * @author haikulou
 */
@Repository
public class InMemoryUserRepository implements UserRepository {

    private static final Map<String, User> USER_STORE = new ConcurrentHashMap<>();

    static {
        User demo = new User("admin", "123456", "管理员");
        USER_STORE.put(demo.getUsername(), demo);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(USER_STORE.get(username));
    }
}
