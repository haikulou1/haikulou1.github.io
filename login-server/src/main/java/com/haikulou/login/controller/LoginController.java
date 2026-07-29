package com.haikulou.login.controller;

import com.haikulou.login.common.Result;
import com.haikulou.login.dto.LoginRequest;
import com.haikulou.login.dto.LoginResponse;
import com.haikulou.login.service.LoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录接口。
 *
 * @author haikulou
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    /**
     * 登录。
     *
     * @param request 登录请求（经参数校验）
     * @return 统一响应体，data 为 {@link LoginResponse}
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = loginService.login(request);
        return Result.success(response);
    }
}
