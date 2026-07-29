package cn.haikulou.auth;

import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

/**
 * 登出入口 Servlet，失效 Token。
 *
 * <p>路由：{@code POST /api/logout}
 * <p>认证：{@code Authorization: Bearer <token>}
 * <p>MVP 无状态模式下，客户端清除 Token 即可，服务端仅记录日志。
 */
public class LogoutServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(LogoutServlet.class.getName());

    private static final int CODE_UNAUTHORIZED = 40100;

    private static final String BEARER_PREFIX = "Bearer ";

    private static final long serialVersionUID = 1L;

    private final Gson gson = new Gson();
    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String token = extractToken(request);
        if (token == null || !authService.verifyToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(gson.toJson(ApiResponse.error(CODE_UNAUTHORIZED, "未登录或登录已过期")));
            return;
        }

        authService.logout(token);
        response.setStatus(HttpServletResponse.SC_OK);
        out.print(gson.toJson(ApiResponse.success(null)));
    }

    /**
     * 从 Authorization 头提取 Bearer Token。
     *
     * @param request HTTP 请求
     * @return Token 字符串，缺失时返回 null
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authHeader.substring(BEARER_PREFIX.length()).trim();
    }
}
