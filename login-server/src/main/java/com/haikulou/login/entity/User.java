package com.haikulou.login.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户实体。
 * <p>
 * 当前为演示用内存实现，未接持久层；后续接入数据库时可替换为 JPA 实体。
 *
 * @author haikulou
 */
@Data
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户名 */
    private String username;
    /** 密码（生产环境应存储加盐哈希，此处仅示例） */
    private String password;
    /** 展示名 */
    private String nickname;

    public User(String username, String password, String nickname) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
    }
}
