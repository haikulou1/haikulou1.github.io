# 架构文档

## 概述

auth-login 是用户登录模块，提供账号密码登录、登录态维持、退出登录与安全防护能力。

## 技术栈

- Spring Boot 3.2.5
- JDK 21
- MyBatis Spring Boot Starter 3.0.3
- MySQL
- spring-security-crypto（BCrypt）

## 分层架构

```
Web 层（Controller） → Service 层 → Manager 层 → DAO 层
```

## 模块列表

| 模块 | 职责 | 模块文档 |
|------|------|----------|
| auth | 用户认证（登录/退出/登录态/安全防护） | docs/modules/auth/README.md |

## 包结构

```
com.antdigital.auth
├── controller        # Web 层
├── service           # Service 层
│   └── impl
├── manager           # Manager 层
│   └── cache
├── dao               # DAO 层
│   └── mapper
├── model             # 数据模型
│   ├── entity        # DO
│   └── dto           # DTO
├── config            # 配置
└── common            # 公共层
    ├── constant
    ├── enums
    ├── exception
    └── model
```

## 约束

- 上层依赖下层，禁止反向依赖与循环依赖。
- 所有用户输入必须校验；SQL 一律参数化 #{}。
- 密码一律 BCrypt 加盐哈希，禁止明文存储。
- 账号不存在与密码错误统一返回「账号或密码错误」，禁止账号枚举。
