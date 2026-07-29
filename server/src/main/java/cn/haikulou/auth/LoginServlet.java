package cn.haikulou.auth;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * 登录入口 Servlet，接收登录请求并返回 Token。
 *
 * <p>路由：{@code POST /api/login}
 * <p>请求体：{@code {"username":"...","password":"..."}}
 * <p>响应：{@link ApiResponse} 封装的 {@link LoginResult} 或错误码
 */
public class LoginServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(LoginServlet.class.getName());

    private static final int CODE_SUCCESS = 0;
    private static final int CODE_PARAM_ERROR = 40001;
    private static final int CODE_AUTH_FAILED = 40101;
    private static final int CODE_SERVER_ERROR = 50000;

    /** 用户名校验：3-32 位字母数字下划线 */
    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_]{3,32}$");

    /** 密码长度：6-64 */
    private static final int PASSWORD_MIN_LEN = 6;
    private static final int PASSWORD_MAX_LEN = 64;

    private static final long serialVersionUID = 1L;

    private final Gson gson = new Gson();
    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // 解析请求体
            LoginRequest loginRequest = parseRequestBody(request);

            // 参数校验
            String validationError = validateInput(loginRequest);
            if (validationError != null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(toJson(ApiResponse.error(CODE_PARAM_ERROR, validationError)));
                return;
            }

            // 认证
            LoginResult result = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
            if (result == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print(toJson(ApiResponse.error(CODE_AUTH_FAILED, "用户名或密码错误")));
                return;
            }

            // 成功
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(toJson(ApiResponse.success(result)));

        } catch (JsonSyntaxException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(toJson(ApiResponse.error(CODE_PARAM_ERROR, "请求体格式错误")));
            LOG.log(Level.WARNING, "登录请求体解析失败: {0}", e.getMessage());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(toJson(ApiResponse.error(CODE_SERVER_ERROR, "服务器内部错误")));
            LOG.log(Level.SEVERE, "登录处理异常: {0}", e.getMessage());
        }
    }

    /**
     * 解析 JSON 请求体为登录请求对象。
     */
    private LoginRequest parseRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        BufferedReader reader = request.getReader();
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return gson.fromJson(sb.toString(), LoginRequest.class);
    }

    /**
     * 前端输入校验，返回错误描述，null 表示通过。
     */
    private String validateInput(LoginRequest loginRequest) {
        if (loginRequest == null) {
            return "请求体不能为空";
        }
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return "用户名和密码不能为空";
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return "用户名需为3-32位字母数字下划线";
        }
        if (password.length() < PASSWORD_MIN_LEN || password.length() > PASSWORD_MAX_LEN) {
            return "密码长度需为6-64位";
        }
        return null;
    }

    private String toJson(Object obj) {
        return gson.toJson(obj);
    }

    /**
     * 登录请求 DTO，仅用于 JSON 反序列化。
     */
    private static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }
}
