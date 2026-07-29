package cn.haikulou.auth;

/**
 * 用户信息视图对象，对外暴露的最小用户字段集，屏蔽 passwordHash 等敏感字段。
 */
public class UserInfo {

    private Long id;

    private String username;

    private String nickname;

    public UserInfo() {
    }

    public UserInfo(Long id, String username, String nickname) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
    }

    /**
     * 从 User 实体构造对外视图对象。
     *
     * @param user 持久层实体
     * @return 不含敏感字段的视图对象
     */
    public static UserInfo fromUser(User user) {
        if (user == null) {
            return null;
        }
        return new UserInfo(user.getId(), user.getUsername(), user.getNickname());
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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
