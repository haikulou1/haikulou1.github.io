# auth 模块文档

## 模块职责

提供用户账号密码登录、登录态维持、退出登录及安全防护（防枚举、暴力破解锁定、IP 限流、验证码）。

## 关键类

| 类 | 说明 |
|----|------|
| AuthController | 登录/退出 HTTP 入口，设置安全 Cookie |
| AuthService / AuthServiceImpl | 认证核心流程 |
| UserMapper | 用户账号数据访问 |
| SessionManager | 会话创建/校验/注销（TTL + 记住我） |
| LoginAttemptManager | 失败计数、账号锁定、IP 限流 |
| CaptchaManager | 验证码生成与校验 |
| UserDO | 用户账号数据对象 |
| LoginRequest / LoginResponse | 登录请求/响应 DTO |
| BusinessException | 业务异常 |
| ResponseCodeEnum | 错误码枚举 |

## 依赖关系

- AuthController → AuthService
- AuthServiceImpl → UserMapper、SessionManager、LoginAttemptManager、CaptchaManager、PasswordEncoder
- 依赖 spring-security-crypto 提供 BCrypt

## API 接口列表

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 账号密码登录，下发会话 Cookie |
| POST | /api/auth/logout | 退出登录，清除会话与 Cookie |

### 登录请求示例

```json
POST /api/auth/login
{
  "username": "alice",
  "password": "secret123",
  "rememberMe": true
}
```

### 登录成功响应

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "a1b2c3...",
    "username": "alice",
    "expiresIn": 604800
  }
}
```

### 登录失败响应（防枚举统一提示）

```json
{
  "code": 500,
  "message": "账号或密码错误",
  "errorCode": "A0002",
  "userTip": "账号或密码错误",
  "data": null
}
```
