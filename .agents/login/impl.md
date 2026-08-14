# 编码报告：用户登录模块

> 产出位置：`.agents/login/impl.md`（用户未显式指定 OUTPUT_FILE，按 skill 默认约定写入）
> 技能：dtazziboot-java-coding-standards
> 工程基线：Spring Boot 3.2.5 + JDK 21 + MyBatis 3.0.3

## 模块进度追踪表

| 序号 | 模块 | READ | TEST | IMPL | CHECK | DOCS | 状态 |
|:----:|------|:----:|:----:|:----:|:-----:|:----:|------|
| 1 | auth（认证） | ✅ | ✅ | ✅ | ✅ | ✅ | 已完成 |

## 各阶段产出摘要

### READ
- 仓库现状：当前 cwd 为静态博客站点，无 Java 工程结构、无 SSOT.md。
- 决策：从零搭建标准 Maven Spring Boot 工程。
- 已加载规范：project-structure / naming / exception-logging / security / mysql / unit-testing / frontend-backend / constants。

### TEST
- 测试文件：`src/test/java/com/antdigital/auth/service/impl/AuthServiceImplTest.java`
- 被测类：`AuthServiceImpl`
- 测试方法数：13
- 覆盖场景：正常路径 ✓、记住我 ✓、账号不存在 ✓、密码错误 ✓、暴力破解锁定 ✓、账号已锁定 ✓、账号禁用 ✓、IP 限流 ✓、验证码缺失 ✓、验证码错误 ✓、验证码正确登录 ✓、退出登录 ✓、空令牌退出 ✓

### IMPL
已实现文件清单见下方「代码变更清单」。

### CHECK
- L1 静态检查：见下表。
- L2 动态验证：⚠️ 跳过。原因：运行环境 exec 工具受 bwrap 沙箱限制（`Operation not permitted`），无法执行 `mvn` 编译与单测。待人工在本地执行验证命令。

## 代码变更清单

### 工程配置
- `pom.xml` — Maven 工程配置（Spring Boot 3.2.5 / JDK 21 / MyBatis / spring-security-crypto / validation）
- `src/main/resources/application.yml` — 应用与登录模块业务配置
- `src/main/resources/schema.sql` — user_account 建表 DDL
- `src/main/resources/mapper/UserMapper.xml` — MyBatis 映射（resultMap + 参数化 #{}）
- `.gitignore`

### 公共层
- `src/main/java/com/antdigital/auth/AuthApplication.java` — 启动类
- `src/main/java/com/antdigital/auth/config/AuthConfiguration.java` — BCryptPasswordEncoder Bean
- `src/main/java/com/antdigital/auth/common/constant/AuthConstants.java` — 登录模块常量
- `src/main/java/com/antdigital/auth/common/enums/ResponseCodeEnum.java` — 错误码枚举（A/B + 4位）
- `src/main/java/com/antdigital/auth/common/exception/BusinessException.java` — 业务异常
- `src/main/java/com/antdigital/auth/common/model/ApiResponse.java` — 统一响应结构

### 模型层
- `src/main/java/com/antdigital/auth/model/entity/UserDO.java` — 用户数据对象
- `src/main/java/com/antdigital/auth/model/dto/LoginRequest.java` — 登录请求（含 Bean Validation）
- `src/main/java/com/antdigital/auth/model/dto/LoginResponse.java` — 登录响应
- `src/main/java/com/antdigital/auth/model/dto/LogoutRequest.java` — 退出请求

### DAO 层
- `src/main/java/com/antdigital/auth/dao/mapper/UserMapper.java` — 用户数据访问接口

### Manager 层
- `src/main/java/com/antdigital/auth/manager/cache/SessionManager.java` — 会话管理（TTL、记住我区分）
- `src/main/java/com/antdigital/auth/manager/cache/LoginAttemptManager.java` — 失败计数/锁定/IP 限流
- `src/main/java/com/antdigital/auth/manager/cache/CaptchaManager.java` — 验证码生成与校验

### Service 层
- `src/main/java/com/antdigital/auth/service/AuthService.java` — 认证服务接口
- `src/main/java/com/antdigital/auth/service/impl/AuthServiceImpl.java` — 认证服务实现（核心流程）

### Web 层
- `src/main/java/com/antdigital/auth/controller/AuthController.java` — 登录/退出控制器（Cookie 安全设置）
- `src/main/java/com/antdigital/auth/controller/GlobalExceptionHandler.java` — 全局异常处理

### 测试
- `src/test/java/com/antdigital/auth/service/impl/AuthServiceImplTest.java` — AuthServiceImpl 单测

## L1 静态检查

| 检查项 | 规范要求 | 符合情况 |
|--------|----------|:--------:|
| 命名规范 | 类名大驼峰、方法名小驼峰、常量全大写下划线 | ✅ |
| 前后端规约 | JSON key lowerCamelCase、统一响应含 errorCode/userTip | ✅ |
| 异常日志 | SLF4J + 占位符、自定义 BusinessException、错误码 5 位 | ✅ |
| 安全规范 | SQL 参数化 #{}、@Valid 输入校验、BCrypt 加盐、防枚举 | ✅ |
| MySQL 规范 | 表名小写、必备 id/gmt_create/gmt_modified、resultMap | ✅ |
| 单元测试 | 测试类存在、AAA 模式、Mockito+AssertJ、镜像包路径 | ✅ |
| 常量规范 | 无魔法值、常量归类、Long 大写 L | ✅ |
| 工程结构 | 接口/实现分离、分层清晰、common 包 | ✅ |

## L2 动态验证

| 验证项 | 状态 | 说明 |
|--------|:----:|------|
| 编译验证 | ⚠️ | 跳过：exec 受 bwrap 沙箱限制，无法执行 mvn |
| 单测验证 | ⚙️ | 待人工执行 |

## 待人工验证

```bash
mvn clean compile -DskipTests
mvn test -Dtest=AuthServiceImplTest
```

## 验收标准映射

| 验收项 | 实现位置 |
|--------|----------|
| 1. 登录入口 POST /auth/login | AuthController#login |
| 2. 输入校验（用户名/密码非空+长度） | LoginRequest @NotBlank/@Size + GlobalExceptionHandler |
| 3. 密码 BCrypt 加盐哈希校验 | AuthServiceImpl + AuthConfiguration#passwordEncoder |
| 4. 账号不存在与密码错误统一提示（防枚举） | AuthServiceImpl 返回 ACCOUNT_OR_PASSWORD_ERROR |
| 5. 暴力破解锁定（失败次数+锁定时长） | LoginAttemptManager + AuthServiceImpl |
| 6. 账号禁用拒绝登录 | AuthServiceImpl ACCOUNT_DISABLED |
| 7. 登录态 Cookie HttpOnly+Secure+SameSite、记住我区分有效期 | AuthController + SessionManager |
| 8. 退出登录清除会话与 Cookie | AuthController#logout + AuthService#logout |

## 已知限制与后续规划

- SessionManager / LoginAttemptManager / CaptchaManager 为内存实现，生产应替换为 Redis。
- 验证码当前为数字验证码内存存储，后续可接入图形验证码服务。
- 后续规划：第三方登录、扫码登录、MFA、SSO（本期不包含）。
