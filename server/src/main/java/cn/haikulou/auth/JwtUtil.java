package cn.haikulou.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JWT 签发与解析工具。
 *
 * <p>采用 HS256 算法，Secret 从环境变量 {@code JWT_SECRET} 读取，
 * 未配置时使用开发默认值（仅限开发环境）。
 */
public final class JwtUtil {

    /** Secret 环境变量名 */
    private static final String ENV_JWT_SECRET = "JWT_SECRET";

    /** 开发期默认 Secret（32 字节，满足 HS256 最低 256-bit 要求），生产必须通过环境变量覆盖 */
    private static final String DEFAULT_SECRET = "haikulou-dev-secret-key-20260729";

    private static final Logger LOG = Logger.getLogger(JwtUtil.class.getName());

    /** Token 有效期：24 小时（毫秒） */
    private static final long EXPIRATION_MS = 24L * 60 * 60 * 1000;

    /** Token 有效期：24 小时（秒），供前端展示 */
    public static final long EXPIRATION_SECONDS = 86400L;

    private JwtUtil() {
        // 工具类禁止实例化
    }

    /**
     * 获取 JWT Secret，优先读环境变量。
     *
     * @return Secret 字符串
     */
    private static String getSecret() {
        String secret = System.getenv(ENV_JWT_SECRET);
        if (secret == null || secret.isEmpty()) {
            LOG.log(Level.WARNING, "JWT_SECRET 环境变量未配置，使用开发默认密钥，生产环境必须配置");
            return DEFAULT_SECRET;
        }
        return secret;
    }

    /**
     * 签发 JWT。
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @return 签发的 JWT 字符串
     */
    public static String generateToken(Long userId, String username) {
        long now = System.currentTimeMillis();
        Date expiry = new Date(now + EXPIRATION_MS);
        return Jwts.builder()
                .claim("userId", userId)
                .claim("username", username)
                .setIssuedAt(new Date(now))
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS256, getSecret())
                .compact();
    }

    /**
     * 解析 JWT，校验签名与有效期。
     *
     * @param token JWT 字符串
     * @return 解析后的 Claims
     * @throws io.jsonwebtoken.JwtException Token 无效或过期时抛出
     */
    public static Claims parseToken(String token) {
        Jws<Claims> jws = Jwts.parser()
                .setSigningKey(getSecret())
                .parseClaimsJws(token);
        return jws.getBody();
    }

    /**
     * 判断 Token 是否已过期。
     *
     * @param token JWT 字符串
     * @return true 表示已过期或无效
     */
    public static boolean isExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Token 解析失败，判定为已过期: {0}", e.getMessage());
            return true;
        }
    }
}
