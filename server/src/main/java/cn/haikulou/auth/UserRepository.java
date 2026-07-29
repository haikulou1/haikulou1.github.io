package cn.haikulou.auth;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 用户数据访问层，基于 H2 内存数据库。
 *
 * <p>采用参数化查询（PreparedStatement）防止 SQL 注入。
 * 数据库随应用启停，初始化时写入默认测试用户。
 */
public class UserRepository {

    private static final Logger LOG = Logger.getLogger(UserRepository.class.getName());

    private static final String JDBC_URL = "jdbc:h2:mem:authdb;DB_CLOSE_DELAY=-1";
    private static final String JDBC_USER = "sa";
    private static final String JDBC_PASSWORD = "";

    private static final String DDL = "CREATE TABLE IF NOT EXISTS user ("
            + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
            + "username VARCHAR(32) NOT NULL UNIQUE,"
            + "password_hash VARCHAR(100) NOT NULL,"
            + "nickname VARCHAR(64),"
            + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
            + ")";

    private static final String SQL_FIND_BY_USERNAME =
            "SELECT id, username, password_hash, nickname, created_at FROM user WHERE username = ?";

    private static final String SQL_FIND_BY_ID =
            "SELECT id, username, password_hash, nickname, created_at FROM user WHERE id = ?";

    private static final String SQL_INSERT_USER =
            "INSERT INTO user (username, password_hash, nickname) VALUES (?, ?, ?)";

    private static volatile boolean initialized = false;

    /**
     * 初始化数据库表与默认用户，仅在首次调用时执行（双检锁保证线程安全）。
     */
    public static synchronized void initDefaultUser() {
        if (initialized) {
            return;
        }
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(DDL);
            // 默认用户 admin/123456，密码以 BCrypt 哈希存储
            String adminHash = BCrypt.hashpw("123456", BCrypt.gensalt(10));
            try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT_USER)) {
                ps.setString(1, "admin");
                ps.setString(2, adminHash);
                ps.setString(3, "管理员");
                ps.executeUpdate();
            }
            initialized = true;
            LOG.info("默认用户 admin 初始化完成");
        } catch (SQLException e) {
            // 区分"表/用户已存在"与严重错误：仅前者标记为已初始化，后者保持 false 允许重试
            int errorCode = e.getErrorCode();
            if (errorCode == 42101 || errorCode == 23505) {
                LOG.log(Level.INFO, "默认用户已存在，跳过初始化: {0}", e.getMessage());
                initialized = true;
            } else {
                LOG.log(Level.SEVERE, "默认用户初始化失败，将在下次请求时重试: {0}", e.getMessage());
            }
        }
    }

    /**
     * 按用户名查询用户。
     *
     * @param username 用户名
     * @return 用户实体，不存在时返回 null
     */
    public User findByUsername(String username) {
        ensureInitialized();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_USERNAME)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return User.fromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "按用户名查询用户失败: {0}", e.getMessage());
        }
        return null;
    }

    /**
     * 按 ID 查询用户。
     *
     * @param id 用户 ID
     * @return 用户实体，不存在时返回 null
     */
    public User findById(Long id) {
        ensureInitialized();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return User.fromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "按ID查询用户失败: {0}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取 H2 数据库连接。
     *
     * @return JDBC 连接
     * @throws SQLException 连接失败
     */
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }

    /**
     * 确保数据库已初始化（延迟初始化）。
     */
    private static void ensureInitialized() {
        if (!initialized) {
            initDefaultUser();
        }
    }
}
