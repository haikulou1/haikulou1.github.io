# Java 接口设计文档（需求澄清）

- **日期**: 2026-07-31
- **阶段**: 需求澄清（需求分析）
- **技能**: brainstorming
- **原始需求**: 用 java 分别写三个接口 helloworld、冒泡排序

---

## 1. 需求理解

用 Java 语言实现对外暴露的接口（REST API），覆盖两个已命名功能：
- `helloworld`：最基础的问候接口。
- `冒泡排序`：对输入数据执行冒泡排序并返回结果。

需求中提到"三个接口"，但仅给出两个名称，第三个接口的功能与命名未在需求中给出。

---

## 2. 仓库现状（项目上下文）

当前仓库为一个**静态网站/博客站点**（Jekyll/Hugo 风格），目录结构如下：

```
.
├── .git
├── 2019/
├── about/
├── archives/
├── atom.xml
├── categories/
├── content.json
├── css/
├── images/
├── index.html
├── js/
├── lib/
├── page/
├── public/
└── tags/
```

**关键事实**：
- 仓库中**不存在任何 Java 工程结构**（无 `pom.xml` / `build.gradle` / `src/main/java`）。
- 本次需求属于**全新引入 Java 技术栈**，与现有静态站点无直接依赖关系。
- 因此 Java 工程应作为**独立模块**新建，不影响现有站点构建链路。

---

## 3. 需求模糊点与澄清决策

> 依据全流水线模式约束，brainstorming 的交互澄清环节由执行引擎**静默接管**：可按行业最佳实践决策的模糊点直接定案；真正信息不足项列出缺失条件，禁止编造。

### 3.1 可定案模糊点（按行业最佳实践决策）

| # | 模糊点 | 决策 | 依据 |
|---|--------|------|------|
| F1 | 技术栈未指定 | 采用 **Spring Boot + Spring MVC REST Controller** | Java REST 行业最佳实践；企业级主流；最小化样板代码 |
| F2 | helloworld 返回格式 | HTTP GET，返回 JSON `{"message":"Hello World"}` | REST 接口惯例：统一 JSON 响应 |
| F3 | 冒泡排序入参形式 | HTTP POST，入参 JSON 体 `{"arr":[3,1,2]}`（整数数组） | POST 适合承载请求体；数组为排序最自然输入 |
| F4 | 冒泡排序方向 | **升序**（ascending）作为默认 | 排序接口默认升序为通用惯例 |
| F5 | 冒泡排序出参 | 返回 JSON `{"sorted":[1,2,3]}` | 与入参对称，便于调用方解析 |
| F6 | 工程结构 | 独立 Maven 模块，标准 `src/main/java` 目录 | 全新引入，独立模块不影响现有站点 |
| F7 | Java 版本 | Java 17（LTS） | Spring Boot 3.x 最低要求，当前 LTS |
| F8 | Spring Boot 版本 | 3.2.x | 当前稳定主线 |

### 3.2 待澄清项（信息不足，禁止编造）

| # | 模糊点 | 状态 | 候选建议（供后续澄清，未定案） |
|---|--------|------|--------------------------------|
| U1 | **第三个接口的名称与功能未指定** | ❌ 缺失 | 需求称"三个接口"，仅给出 2 个名称。候选方向（仅供参考，**未编造定论**）：① 数组求和/统计接口；② 快速排序接口（与冒泡排序对比）；③ 回文判断接口。需用户明确后纳入实现范围。 |

---

## 4. 技术方案选型（brainstorming：提出 2-3 个方案）

### 方案 A（推荐）：Spring Boot REST Controller

- 用 Spring Boot 3.2.x + Spring MVC，定义 `@RestController`，每个接口一个 `@GetMapping`/`@PostMapping`。
- 优点：企业级主流、生态成熟、JSON 序列化开箱即用、易于扩展。
- 缺点：引入 Spring Boot 依赖（对全新工程无负担）。

### 方案 B：纯 Java（main 方法 + 控制台）

- 不使用任何框架，用 `public static void main` 直接演示 helloworld 与冒泡排序逻辑。
- 优点：零依赖、最简单。
- 缺点：**不是"接口"**（无 HTTP 暴露），与需求"写接口"语义不符；不可被外部调用。

### 方案 C：Java + 轻量 HTTP（com.sun.net.httpserver）

- 用 JDK 自带 `com.sun.net.httpserver.HttpServer` 暴露 HTTP，不引第三方依赖。
- 优点：零第三方依赖、仍为 HTTP 接口。
- 缺点：JSON 需手写、样板代码多、非企业标准。

**推荐方案 A**：需求明确"接口"，方案 A 最契合语义且为行业最佳实践；方案 B 不满足"接口"定义；方案 C 虽零依赖但偏离企业标准。

---

## 5. 接口设计（已定案部分）

### 5.1 接口清单

| 接口 | 方法 | 路径 | 入参 | 出参 | 状态 |
|------|------|------|------|------|------|
| helloworld | GET | `/api/helloworld` | 无 | `{"message":"Hello World"}` | ✅ 已定案 |
| 冒泡排序 | POST | `/api/bubble-sort` | `{"arr":[3,1,2]}` | `{"sorted":[1,2,3]}` | ✅ 已定案 |
| 第三个接口 | TBD | TBD | TBD | TBD | ❌ 待澄清（U1） |

### 5.2 契约示例

**helloworld**
```
GET /api/helloworld
HTTP 200
{"message":"Hello World"}
```

**冒泡排序**
```
POST /api/bubble-sort
Content-Type: application/json
请求体: {"arr":[5,2,8,1,9]}
HTTP 200
{"sorted":[1,2,5,8,9]}
```

### 5.3 冒泡排序算法约束

- 算法必须为**冒泡排序**（相邻元素两两比较交换），不得替换为 `Arrays.sort` 等内置排序。
- 升序排列。
- 输入为整数数组；空数组返回空数组；单元素原样返回。
- 边界：`arr` 为 null → 返回 400 错误（参数缺失）。

---

## 6. 工程结构规划（预览，本阶段不创建代码）

```
java-interfaces/                         # 独立 Maven 模块
├── pom.xml                              # Spring Boot 3.2.x, Java 17
└── src/main/java/com/example/demo/
    ├── DemoApplication.java             # @SpringBootApplication 启动类
    └── controller/
        ├── HelloWorldController.java   # GET /api/helloworld
        └── BubbleSortController.java    # POST /api/bubble-sort
```

> 本阶段（需求澄清）**不创建上述代码文件**，仅作为设计预览。代码实现将在「编码实现」阶段执行。

---

## 7. 待澄清清单（阻塞项）

1. **U1 — 第三个接口的名称与功能**：需求称"三个接口"但仅给出两个名称。请明确第三个接口的需求：
   - 名称？
   - 功能？
   - 入参/出参契约？
   - 若"三个"为笔误，实际只需两个接口，请确认。

---

## 8. 设计产物清点

| 产物 | 路径 | 类型 | 状态 |
|------|------|------|------|
| 需求澄清与设计文档 | `docs/superpowers/specs/2026-07-31-java-interfaces-design.md` | Markdown 文档 | ✅ 已生成 |

**代码变更清单**：无（需求澄清阶段，禁止修改代码文件）。

**下一步**：待 U1（第三个接口需求）澄清后，进入「编码实现」阶段，按方案 A（Spring Boot）创建 `java-interfaces/` 独立模块并实现接口。
