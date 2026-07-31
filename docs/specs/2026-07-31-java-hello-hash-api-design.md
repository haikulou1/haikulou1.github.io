# Java HelloWorld 与哈希算法接口 — 设计文档

> 阶段：需求澄清（brainstorming 产出）
> 日期：2026-07-31
> 技能：/brainstorming
> 状态：设计已定稿，待进入「编码实现」阶段落地

---

## 1. 背景与需求

原始需求：用 Java 分别写 2 个接口 —— ① `helloworld`；② `哈希算法`。

## 2. 项目上下文现状

- 当前仓库为**纯静态博客/网站仓库**：根目录含 `index.html`、`atom.xml`、`css/`、`js/`、`lib/`、`2019/`、`archives/`、`categories/`、`tags/`、`page/`、`public/`、`about/`、`images/`、`content.json`。
- 最近提交：`77aa0ea Site updated: 2019-10-10 21:28:57`。
- **全仓搜索 `.java$|pom\.xml|build\.gradle` 命中 0**（扫描 111 文件），即**仓库无任何 Java/Maven/Gradle 工程结构**。
- 结论：需求与现有技术栈不匹配，须从零新建独立 Java 模块，且不得污染现有静态站点产物。

## 3. 澄清的关键歧义与自主决策

| # | 歧义点 | 决策 | 依据 |
|---|--------|------|------|
| Q1 | 仓库无 Java 工程，代码放哪 | 仓库根下新建独立子目录 `java-api-demo/`（Maven 工程），与博客静态文件物理隔离 | 风险最低/改动最小 |
| Q2 | "接口"语义 | HTTP REST API（Spring Boot `@RestController`） | 典型 Web API 场景；行业最佳实践 |
| Q3 | HelloWorld 返回格式 | JSON `{"message":"Hello World"}` | 主流、可测 |
| Q4 | 哈希算法选哪种 | 默认 SHA-256，`algorithm` 参数可选 `md5/sha-256/sha-512`；用 JDK `MessageDigest`（零三方依赖） | YAGNI + 安全主流 |
| Q5 | 哈希接口入参形式 | `GET`，query 参数 `algorithm`+`input` | 最简、幂等、易测 |
| Q6 | 技术栈/构建工具 | Spring Boot 3.x + Java 17 LTS + Maven | 约定优于配置、生态成熟 |

> 说明：本任务处于全自动流水线模式，澄清歧义由引擎按优先级（已验证事实 > 风险最低/改动最小 > 行业最佳实践）静默接管决策，未阻塞等待人工确认。

## 4. 方案对比（brainstorming: Propose 2-3 approaches）

| 方案 | 技术栈 | 优点 | 缺点 | 结论 |
|------|--------|------|------|------|
| **A. Spring Boot REST** | Spring Boot 3 + Java 17 + Maven | 主流、生产级、约定优于配置、生态成熟 | 需 `spring-boot-starter-web` 依赖 | ✅ 选定 |
| B. 纯 Java SE HttpServer | `com.sun.net.httpserver` + Java SE | 零依赖、单文件 | `com.sun.*` 非主流生产方案、手写路由 | 否决 |
| C. Servlet + 外部 Tomcat | Servlet API + Tomcat | 传统标准 | 配置重、不符合现代最佳实践 | 否决 |

**选定 A 的理由**："接口"强暗示 HTTP API 框架场景；Spring Boot 为 Java Web API 行业最佳实践；起步依赖最小（单 `starter-web`）；与 Q2–Q6 决策天然契合。

## 5. 接口设计规格

### 5.1 接口一：HelloWorld

| 项 | 值 |
|----|----|
| 路径 | `GET /api/hello` |
| 入参 | 无 |
| 成功响应 | `200 OK`，`Content-Type: application/json`，body：`{"message":"Hello World"}` |
| 示例 | `curl http://localhost:8080/api/hello` → `{"message":"Hello World"}` |

### 5.2 接口二：哈希算法

| 项 | 值 |
|----|----|
| 路径 | `GET /api/hash` |
| 入参 | `algorithm`（可选，默认 `sha-256`，可选 `md5`/`sha-256`/`sha-512`）；`input`（必填，待哈希文本） |
| 成功响应 | `200`，body：`{"algorithm":"sha-256","input":"abc","hash":"<hex 摘要>"}` |
| 非法 algorithm | `400`，body：`{"error":"unsupported algorithm: <x>"}` |
| 缺失 input | `400`，body：`{"error":"input parameter is required"}` |
| 实现 | JDK `java.security.MessageDigest.getInstance(algorithm)`；结果转 hex（小写） |
| 示例 | `curl 'http://localhost:8080/api/hash?algorithm=sha-256&input=abc'` |

## 6. 工程结构（编码阶段将创建，本阶段不落地）

```
java-api-demo/                      # 独立 Maven 工程根，与博客文件隔离
├── pom.xml                         # Spring Boot 3.x + Java 17，spring-boot-starter-web
└── src
    └── main
        ├── java
        │   └── com
        │       └── example
        │           └── demo
        │               ├── Application.java        # @SpringBootApplication 入口
        │               ├── controller
        │               │   ├── HelloController.java   # GET /api/hello
        │               │   └── HashController.java    # GET /api/hash
        │               └── service
        │                   └── HashService.java        # MessageDigest 封装
        └── resources
            └── application.yml                          # server.port=8080
```

- 依赖：`spring-boot-starter-web`（唯一起步依赖，含嵌入式 Tomcat）。
- 测试（可选，编码阶段补）：`HelloControllerTest`、`HashServiceTest`（MockMvc / 纯服务单测）。

## 7. 非目标（YAGNI）

- 不做认证/鉴权、不做持久化、不做日志框架配置、不做参数长度限制中间件。
- 不引入第三方哈希库（BouncyCastle 等），仅用 JDK `MessageDigest`。
- 不改造/迁移现有静态博客仓库任何文件。

## 8. 风险与约束

- **环境风险**：本仓库为 2019 年静态博客，运行/构建环境是否已具备 Java 17 + Maven 待编码阶段验证；若缺失，按「测试验证降级协议」转为静态代码审查。
- **隔离约束**：Java 工程必须限定在 `java-api-demo/` 子目录，禁止写入仓库根的任何博客文件。
- **Git 只读**：全程不执行任何 Git 写操作。

## 9. 验收标准

1. `java-api-demo/` 为独立可构建 Maven 工程，`mvn -pl java-api-demo clean package` 成功。
2. `GET /api/hello` 返回 `{"message":"Hello World"}`（200）。
3. `GET /api/hash?algorithm=sha-256&input=abc` 返回正确 SHA-256 hex 摘要（与 `sha256sum` 一致）。
4. 非法 `algorithm` 返回 400；缺失 `input` 返回 400。
5. 未修改任何现有博客静态文件（`git status` 仅显示 `java-api-demo/` 与本文档新增）。

## 10. 待编码阶段执行清单（本阶段不执行）

- [ ] 创建 `java-api-demo/pom.xml`
- [ ] 创建 `Application.java`
- [ ] 创建 `HelloController.java`
- [ ] 创建 `HashController.java` + `HashService.java`
- [ ] 创建 `application.yml`
- [ ] 补充单元测试并验证构建（受 5 分钟超时与降级协议约束）
