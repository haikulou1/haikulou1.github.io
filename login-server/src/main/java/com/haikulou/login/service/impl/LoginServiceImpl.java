package com.haikulou.login.service.impl;

import com.haikulou.login.common.BusinessException;
import com.haikulou.login.common.ResultCode;
import com.haikulou.login.dao.UserRepository;
import com.haikulou.login.dto.LoginRequest;
import com.haikulou.login.dto.LoginResponse;
import com.haikulou.login.entity.User;
import com.haikulou.login.service.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 登录服务实现。
 * <p>
 * 校验用户名密码后颁发一个简易 token（演示用 UUID）。
 *
 * @author haikulou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final UserRepository userRepository;

    @Override
    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();
        log.info("用户登录开始, username={}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("用户不存在, username={}", username);
                    return new BusinessException(ResultCode.UNAUTHORIZED);
                });

        if (!user.getPassword().equals(request.getPassword())) {
            log.warn("密码错误, username={}", username);
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 演示用 token：UUID；生产环境应使用 JWT 等方案
        String token = UUID.randomUUID().toString().replace("-", "");
        log.info("用户登录成功, username={}", username);

        return new LoginResponse(token);
    }
}
