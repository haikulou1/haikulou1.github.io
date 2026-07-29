package com.haikulou.login.service;

import com.haikulou.login.dto.LoginRequest;
import com.haikulou.login.dto.LoginResponse;

/**
 * 登录服务。
 *
 * @author haikulou
 */
public interface LoginService {

    /**
     * 登录。
     *
     * @param request 登录请求
     * @return 登录响应（含 token）
     */
    LoginResponse login(LoginRequest request);
}
