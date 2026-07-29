# login-server 登录服务

Spring Boot 3 后端登录服务，与 `leecode` 仓库的 `login-web` 前端配套。

## 工程结构

```
login-server/
├── pom.xml
└── src/main/
    ├── java/com/haikulou/login/
    │   ├── LoginServerApplication.java        # 启动类
    │   ├── controller/LoginController.java     # 登录接口
    │   ├── service/LoginService.java          # 服务接口
    │   ├── service/impl/LoginServiceImpl.java  # 服务实现
    │   ├── dao/UserRepository.java            # 仓储接口
    │   ├── dao/InMemoryUserRepository.java     # 内存仓储实现
    │   ├── entity/User.java                   # 用户实体
    │   ├── dto/LoginRequest.java              # 登录请求 DTO
    │   ├── dto/LoginResponse.java             # 登录响应 DTO
    │   ├── common/Result.java                 # 统一响应体
    │   ├── common/ResultCode.java             # 响应码枚举
    │   ├── common/BusinessException.java      # 业务异常
    │   ├── common/GlobalExceptionHandler.java # 全局异常处理
    │   └── config/WebConfig.java              # 跨域配置
    └── resources/application.yml
```

## 接口契约

- `POST /api/login`
- Content-Type：`application/json`
- 请求体：
  ```json
  { "username": "admin", "password": "123456" }
  ```
- 成功响应：
  ```json
  { "code": 200, "message": "success", "data": { "token": "xxx" } }
  ```
- 失败响应（用户名或密码错误）：
  ```json
  { "code": 401, "message": "用户名或密码错误", "data": null }
  ```
- 参数校验失败：
  ```json
  { "code": 400, "message": "用户名不能为空", "data": null }
  ```

## 运行方式

```bash
cd login-server
mvn spring-boot:run
```

服务启动后监听 `8080` 端口。演示账号：`admin` / `123456`。

## 设计说明

- 分层遵循 MVC：controller → service → dao。
- 统一响应 `Result<T>`，错误码集中 `ResultCode` 枚举。
- 全局异常 `GlobalExceptionHandler` 捕获业务异常、参数校验异常、兜底异常。
- 当前用户数据为内存实现（`InMemoryUserRepository`），后续可替换为 JPA/MyBatis。
- token 为演示用 UUID，生产环境建议替换为 JWT。
