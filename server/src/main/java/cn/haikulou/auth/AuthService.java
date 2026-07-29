package cn.haikulou.auth;

import io.jsonwebtoken.Claims;
import org.mindrot.jbcrypt.BCrypt;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 认证业务服务，负责密码校验、Token 生成与验证。
 *
 * <p>属于业务层，协调 {@link UserRepository}（数据层）与 {@link JwtUtil}（工具层）。
 */
public class AuthService {

    private static final Logger LOG = Logger.getLogger(AuthService.class.getName());

    private final UserRepository userRepository;

    public AuthService() {
        this.userRepository = new UserRepository();
    }

    /**
     * 登录认证。
     *
     * <p>流程：查询用户 → BCrypt 校验密码 → 生成 JWT。
     *
     * @param username 用户名
     * @param password 明文密码
     * @return 登录结果（含 Token 与用户信息），认证失败返回 null
     */
    public LoginResult login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            LOG.info("登录失败：用户不存在, username=" + username);
            return null;
        }
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            LOG.info("登录失败：密码不匹配, username=" + username);
            return null;
        }
        String token = JwtUtil.generateToken(user.getId(), user.getUsername());
        UserInfo userInfo = UserInfo.fromUser(user);
        LOG.info("登录成功: " + user);
        return new LoginResult(token, JwtUtil.EXPIRATION_SECONDS, userInfo);
    }

    /**
     * 验证 Token 有效性。
     *
     * @param token JWT 字符串
     * @return true 表示 Token 有效且未过期
     */
    public boolean verifyToken(String token) {
        try {
            JwtUtil.parseToken(token);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Token 验证失败: {0}", e.getMessage());
            return false;
        }
    }

    /**
     * 从 Token 提取用户 ID 并查询当前用户信息。
     *
     * @param token JWT 字符串
     * @return 用户信息视图对象，Token 无效时返回 null
     */
    public UserInfo getCurrentUser(String token) {
        try {
            Claims claims = JwtUtil.parseToken(token);
            Long userId = claims.get("userId", Long.class);
            if (userId == null) {
                return null;
            }
            User user = userRepository.findById(userId);
            return UserInfo.fromUser(user);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "获取当前用户失败: {0}", e.getMessage());
            return null;
        }
    }

    /**
     * 登出。
     *
     * <p>MVP 无状态模式：JWT 无法服务端主动失效，客户端清除 Token 即可。
     * 预留黑名单接口供后续迭代。
     *
     * @param token JWT 字符串
     */
    public void logout(String token) {
        LOG.info("用户登出, token 前缀=" + (token == null ? "null" : token.substring(0, Math.min(16, token.length()))));
    }
}
