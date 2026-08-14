package com.antdigital.auth.controller;

import com.antdigital.auth.common.constant.AuthConstants;
import com.antdigital.auth.common.model.ApiResponse;
import com.antdigital.auth.model.dto.LoginRequest;
import com.antdigital.auth.model.dto.LoginResponse;
import com.antdigital.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器，提供登录与退出登录入口。
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @Value("${auth.login.cookie-secure:false}")
    private boolean cookieSecure;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 登录接口。
     *
     * @param request  登录请求
     * @param httpRequest  HTTP 请求
     * @param httpResponse HTTP 响应
     * @return 登录响应
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                            HttpServletRequest httpRequest,
                                            HttpServletResponse httpResponse) {
        String clientIp = resolveClientIp(httpRequest);
        LoginResponse response = authService.login(request, clientIp);

        // 设置登录态 Cookie：HttpOnly + Secure + SameSite
        ResponseCookie cookie = ResponseCookie.from(AuthConstants.SESSION_COOKIE_NAME,
                        response.getToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(AuthConstants.COOKIE_SAMESITE)
                .path("/")
                .maxAge(response.getExpiresIn())
                .build();
        httpResponse.addHeader("Set-Cookie", cookie.toString());

        return ApiResponse.success(response, AuthConstants.MSG_LOGIN_SUCCESS);
    }

    /**
     * 退出登录接口。
     *
     * @param httpRequest  HTTP 请求
     * @param httpResponse HTTP 响应
     * @return 退出结果
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest httpRequest,
                                    HttpServletResponse httpResponse) {
        String token = extractToken(httpRequest);
        authService.logout(token);

        // 清除登录态 Cookie
        Cookie cookie = new Cookie(AuthConstants.SESSION_COOKIE_NAME, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        httpResponse.addCookie(cookie);

        return ApiResponse.success(null, AuthConstants.MSG_LOGOUT_SUCCESS);
    }

    /**
     * 从请求中提取会话令牌（优先 Cookie，其次 Header）。
     *
     * @param request HTTP 请求
     * @return 会话令牌，不存在返回 null
     */
    private String extractToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (AuthConstants.SESSION_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    /**
     * 解析客户端真实 IP。
     *
     * @param request HTTP 请求
     * @return 客户端 IP
     */
    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            int comma = ip.indexOf(',');
            return comma > 0 ? ip.substring(0, comma).trim() : ip.trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank()) {
            return ip.trim();
        }
        return request.getRemoteAddr();
    }
}
