package cn.haikulou.auth;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * 用户实体，存储登录凭据。
 *
 * <p>对应 H2 表 {@code user}，字段采用小写下划线命名，
 * Java 属性采用大驼峰命名。
 */
public class User {

    /** 主键，自增 */
    private Long id;

    /** 用户名，唯一索引 */
    private String username;

    /** 密码哈希（BCrypt），禁止明文 */
    private String passwordHash;

    /** 昵称，展示用 */
    private String nickname;

    /** 创建时间 */
    private Timestamp createdAt;

    public User() {
    }

    public User(Long id, String username, String passwordHash, String nickname, Timestamp createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.createdAt = createdAt;
    }

    /**
     * 从 ResultSet 构造 User，避免散落的列名映射逻辑。
     *
     * @param rs 查询结果集
     * @return 填充后的 User 实例
     * @throws SQLException 列读取失败
     */
    public static User fromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.id = rs.getLong("id");
        user.username = rs.getString("username");
        user.passwordHash = rs.getString("password_hash");
        user.nickname = rs.getString("nickname");
        user.createdAt = rs.getTimestamp("created_at");
        return user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{"
                + "id=" + id
                + ", username='" + username + '\''
                + ", nickname='" + nickname + '\''
                + ", createdAt=" + createdAt
                + '}';
    }
}
