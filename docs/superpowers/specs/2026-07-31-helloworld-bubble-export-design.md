# 设计文档：HelloWorld + 冒泡排序 + 导出功能

- **日期**：2026-07-31
- **阶段**：需求澄清 / 系分设计
- **状态**：已定稿（全流水线模式，静默接管决策权，不阻塞等待人工确认）
- **关联技能**：brainstorming

---

## 1. 背景与目标

### 1.1 需求原文
> 用 java 分别写 2 个接口 helloworld、冒泡排序；前端新增一个页面，有 2 个 tab 分别展示不同的执行结果；新增导出按钮，后台提供导出接口，支持导出各个页面的展示结果。

### 1.2 目标
1. 后端（Java）：提供 `helloworld` 与 `冒泡排序` 两个业务接口，以及一个导出接口。
2. 前端：新增一个页面，包含 2 个 Tab，分别展示上述两个接口的执行结果。
3. 导出：页面上提供「导出」按钮，点击后调用后台导出接口，将当前 Tab 的展示结果导出为 Excel（xlsx）文件。

### 1.3 成功标准
- 启动后端后，三个 HTTP 接口均可被访问并返回正确结果。
- 打开前端页面，可在两个 Tab 间切换并分别看到 helloworld 文本与冒泡排序结果。
- 在任一 Tab 下点击「导出」，可下载对应结果的 xlsx 文件，内容与页面展示一致。

---

## 2. 现状分析

当前仓库根目录为 **Hexo 静态博客（NexT 主题）的生成产物**：

- 存在：`index.html`、`css/`、`js/`、`lib/`、`images/`、`page/`、`tags/`、`archives/` 等静态资源。
- 不存在：任何 Java 后端工程（无 `pom.xml` / `build.gradle` / `src/main/java`）。
- 不存在：任何前端工程化工具链（无 `package.json` / `node_modules` / Vue / React 配置）。
- Git 历史：最近提交均为 `Site updated: 2019-xx-xx`，为静态站点发布记录。

**结论**：本次需求实质是「在此仓库内从零搭建一个 Java 后端 + 一个前端页面 + 导出能力」。后端与前端均为新增工程，不破坏现有静态站点。

---

## 3. 技术选型（2-3 方案对比）

### 3.1 后端框架
| 候选 | 结论 |
|---|---|
| Spring Boot 3 + Maven（✅采用） | Java 行业最佳实践，内嵌 Tomcat，开箱即用 REST，生态成熟 |
| 原生 Servlet + 手写 | 无框架依赖，但样板代码多，不符合行业惯例 |
| Quarkus / Micronaut | 优秀但小众，团队认知成本高 |

**决策**：Spring Boot 3.x + Maven，JDK 17。

### 3.2 前端形态
| 候选 | 结论 |
|---|---|
| 单个静态 HTML + 原生 JS + fetch（✅采用） | 现有仓库即静态 HTML 站点，改动最小，契合架构惯例；需求简单，原生 Tab 足够 |
| Vue3 + Vite 工程 | 需引入整套构建链，与 Hexo 产物风格冲突，过度设计（违反 YAGNI） |
| Thymeleaf 服务端渲染 | Tab 切换交互体验弱，非纯前端 |

**决策**：新增一个独立静态 HTML 页面，使用原生 JS 实现 Tab 切换与接口调用。

### 3.3 导出方案
| 候选 | 结论 |
|---|---|
| Apache POI 生成 xlsx（✅采用） | 企业导出 Excel 的标准做法，支持表格/样式 |
| 导出 CSV | 轻量但非"Excel 文件"，体验弱 |
| 前端纯 JS 导出（SheetJS） | 后台不参与，违反需求"后台提供导出接口" |

**决策**：后端使用 Apache POI 生成 xlsx，通过 HTTP 响应流下载。

### 3.4 决策依据
自主决策优先级：① 现有仓库为静态 HTML 站点（已验证事实）→ ② 改动最小、符合现有架构惯例（原生 HTML + Spring Boot 行业标准）→ ③ 行业最佳实践（POI 导出 xlsx）。

---

## 4. 架构设计

```
┌─────────────────────────┐        HTTP/JSON         ┌──────────────────────────────┐
│  前端页面 (静态 HTML/JS)  │  ───────────────────►   │  Spring Boot 后端 (Java)       │
│  demo.html              │  ◄───────────────────    │  HelloController              │
│  - Tab1: HelloWorld     │   GET /api/hello         │  SortController               │
│  - Tab2: 冒泡排序        │   POST /api/bubble-sort  │  ExportController             │
│  - 导出按钮              │   GET /api/export?type=  │  (Apache POI)                 │
└─────────────────────────┘                          └──────────────────────────────┘
```

- 前端为纯静态资源，可由 Spring Boot 静态资源目录托管，或独立部署。
- 后端为单体 Spring Boot 应用，三个 Controller 分离职责，导出能力独立成 Controller。
- 前后端通过 REST + JSON 交互；导出走二进制流（xlsx）下载。

### 4.1 工程目录结构（实现阶段产物，本次仅规划，不创建代码）

```
backend/                              # 新增：Java 后端工程
├── pom.xml                           # Maven 依赖：spring-boot-starter-web + POI
└── src/main/
    ├── java/com/example/demo/
    │   ├── DemoApplication.java      # 启动类
    │   ├── controller/
    │   │   ├── HelloController.java        # GET /api/hello
    │   │   ├── SortController.java         # POST /api/bubble-sort
    │   │   └── ExportController.java       # GET /api/export
    │   ├── service/
    │   │   ├── HelloService.java
    │   │   ├── SortService.java            # 冒泡排序算法
    │   │   └── ExportService.java          # POI 生成 xlsx
    │   └── model/
    │       ├── SortResult.java             # 排序结果 DTO
    │       └── R.java                       # 统一响应体
    └── resources/
        └── application.yml                 # 端口/CORS 配置

demo.html                            # 新增：前端页面（根目录，静态托管）
```

> 说明：当前阶段为需求澄清/系分设计，上述代码文件**不在本次创建**，留待「编码实现」阶段落地。

---

## 5. 组件设计（隔离与清晰）

每个单元单一职责、接口明确、可独立测试：

| 组件 | 职责 | 对外接口 | 依赖 |
|---|---|---|---|
| `HelloController` | 返回 helloworld 文本 | `GET /api/hello` | `HelloService` |
| `HelloService` | 生成 helloworld 内容 | `hello()` | 无 |
| `SortController` | 接收数组，返回排序结果 | `POST /api/bubble-sort` | `SortService` |
| `SortService` | 执行冒泡排序算法 | `bubbleSort(int[])` | 无（纯算法） |
| `ExportController` | 按类型导出对应结果为 xlsx | `GET /api/export?type=` | `ExportService` + `HelloService`/`SortService` |
| `ExportService` | 用 POI 构造 xlsx 字节流 | `buildExcel(type)` | POI |
| 前端 `demo.html` | Tab 切换、展示、触发导出 | DOM 事件 | fetch API |

- `SortService` 为纯算法，无 IO 依赖，最易单元测试。
- `ExportService` 依赖业务 Service 取数据 + POI 构造文件，职责单一。
- Controller 仅做协议适配，不含业务逻辑。

---

## 6. 接口设计（API 契约）

### 6.1 HelloWorld 接口
- **方法**：`GET /api/hello`
- **请求**：无参数
- **响应**（200）：
```json
{ "code": 0, "msg": "ok", "data": "Hello, World!" }
```

### 6.2 冒泡排序接口
- **方法**：`POST /api/bubble-sort`
- **请求体**：
```json
{ "arr": [5, 2, 8, 1, 9, 3] }
```
- **响应**（200）：
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "original": [5, 2, 8, 1, 9, 3],
    "sorted":   [1, 2, 3, 5, 8, 9],
    "steps":    [[2,5,8,1,9,3], [2,5,1,8,9,3], ...]
  }
}
```
- `steps` 为每轮交换后的中间态数组，便于前端展示排序过程（满足"展示执行结果"的丰富性）。
- 输入校验：`arr` 非空且为整数数组；空数组返回 `sorted=[]`。

### 6.3 导出接口
- **方法**：`GET /api/export?type={hello|bubble}`
- **请求参数**：`type` = `hello` 或 `bubble`
- **响应**（200）：
  - `Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
  - `Content-Disposition: attachment; filename="hello.xlsx"` 或 `bubble.xlsx`
  - Body：xlsx 二进制流
- **导出内容**（单列方案，与页面展示对齐，详见 §7.3）：
  - `type=hello`：xlsx 单元格（A1）值为 `Hello, World!`。
  - `type=bubble`：表头 `索引 | 排序后值`，逐行展示 `sorted` 数组的每个元素。
- **数据来源说明**：导出不接受前端传入的数组参数，后端使用固定示例数组 `[5,2,8,1,9,3]`（与前端默认一致）生成冒泡结果，保证接口幂等与简单；详见 §8.3。
- **错误**：`type` 非法 → 400 `{ code: 400, msg: "invalid type" }`。

### 6.4 统一响应体 `R<T>`
```json
{ "code": 0, "msg": "ok", "data": <T> }
```
- `code=0` 成功；非 0 失败。

---

## 7. 前端页面设计

### 7.1 页面结构
- 文件：`demo.html`（仓库根目录，作为新增静态页面，不改动现有 `index.html`）。
- 两个 Tab：
  - **Tab 1「HelloWorld」**：点击/加载后调用 `GET /api/hello`，展示返回文本。
  - **Tab 2「冒泡排序」**：提供一个输入框（默认示例数组 `[5,2,8,1,9,3]`）+「执行」按钮，调用 `POST /api/bubble-sort`，展示 `original`、`sorted` 及可选 `steps`。
- **导出按钮**：位于页面顶部或当前 Tab 内，点击调用 `GET /api/export?type=<当前Tab>`，触发浏览器下载。

### 7.2 交互与展示
- Tab 切换用原生 JS（`classList` 控制 `active` 显隐），无需框架。
- 接口调用用 `fetch`，错误时在页面展示错误信息。
- 导出用 `window.location` 或 `<a download>` 触发文件下载。

### 7.3 导出内容与页面展示对齐
- HelloWorld 导出：xlsx 含单元格 `Hello, World!`，与 Tab1 展示一致。
- 冒泡排序导出：xlsx 表头 `索引 | 排序后值`，逐行展示 `sorted` 数组元素，与 Tab2 展示一致。（选择"排序后值"单列方案，使导出与展示语义统一；`steps` 仅前端展示不导出，避免文件膨胀。）

### 7.4 跨域
- 若前端由独立静态服务托管，后端 `application.yml` 配置全局 CORS 允许该来源；若由 Spring Boot 静态资源托管则同源无需 CORS。决策：**前端页面放入 Spring Boot 静态资源目录（`src/main/resources/static/demo.html`）同源托管**，零 CORS 成本，部署最简。根目录 `demo.html` 作为副本/入口可选。

---

## 8. 数据流

### 8.1 HelloWorld
```
用户点击 Tab1 → 前端 fetch GET /api/hello → HelloController → HelloService.hello() → "Hello, World!" → R 包装 → JSON 返回 → 前端渲染文本
```

### 8.2 冒泡排序
```
用户输入数组点执行 → 前端 fetch POST /api/bubble-sort {arr} → SortController → SortService.bubbleSort(arr) → {original, sorted, steps} → R 包装 → JSON 返回 → 前端渲染表格
```

### 8.3 导出
```
用户点导出(当前Tab) → 前端 GET /api/export?type=hello|bubble → ExportController → ExportService.buildExcel(type)
  ├─ type=hello → HelloService.hello()
  └─ type=bubble → SortService.bubbleSort(默认示例数组)
→ POI 构造 xlsx → 设置 Content-Disposition → 二进制流响应 → 浏览器下载
```

> 导出冒泡时，后端使用一个固定示例数组（与前端默认一致 `[5,2,8,1,9,3]`）生成结果，保证"导出各页面展示结果"语义一致。若需导出用户当前输入的数组，可作为后续增强（见 §11），本次不纳入以保持接口幂等与简单。

---

## 9. 错误处理

| 场景 | 处理 |
|---|---|
| `/api/bubble-sort` 的 `arr` 为空/非数组 | 返回 400，`R{code:400, msg:"arr must be a non-empty integer array"}` |
| 数组元素非整数 | 返回 400，`msg:"arr elements must be integers"` |
| `/api/export?type=` 非法 | 返回 400，`R{code:400, msg:"invalid type"}` |
| 后端未捕获异常 | 全局 `@RestControllerAdvice` 捕获，返回 500，`R{code:500, msg:"server error"}` |
| 前端 fetch 失败 | 页面展示"接口调用失败，请检查后端服务" |

- 统一异常处理类 `GlobalExceptionHandler` 兜底，避免堆栈泄漏。

---

## 10. 测试策略

### 10.1 后端单元测试（JUnit5）
- `SortServiceTest`：
  - 普通数组排序正确。
  - 空数组返回空。
  - 已排序数组不变。
  - 逆序数组排序正确。
  - 含重复元素稳定。
- `HelloServiceTest`：返回值等于 `"Hello, World!"`。
- `ExportServiceTest`：生成的 xlsx 字节非空，且可被 POI 重新读取校验内容。

### 10.2 后端接口测试（Spring MockMvc）
- `HelloControllerTest`：`GET /api/hello` 返回 200 且 `data` 为目标文本。
- `SortControllerTest`：合法请求返回 200 且 `sorted` 正确；非法请求返回 400。
- `ExportControllerTest`：`type=hello|bubble` 返回 200 且 `Content-Type` 为 xlsx；非法 `type` 返回 400。

### 10.3 前端
- 手动验证：Tab 切换、接口结果展示、导出下载文件内容正确。
- 前端无自动化测试框架（YAGNI，静态页面）。

### 10.4 构建可用性降级（遵循任务防超时协议）
- 仅对本次变更涉及的 `backend/` 模块执行 `mvn test`，禁止全量无关构建。
- 若同模块编译/测试 ≥2 次失败、报错文件不在变更范围、单次 >120s、或已执行 1 次 `mvn clean install`，则切换为静态代码审查。

---

## 11. 范围与非目标（YAGNI）

- **不含**：用户认证/鉴权、数据库持久化、前端构建工具链、Vue/React、多语言 i18n、导出 PDF、导出用户实时输入数组的冒泡结果（导出用固定示例数组，见 §8.3）。
- **可扩展点（后续迭代，本次不做）**：导出携带 `steps`、导出用户当前输入数组、多格式导出（csv/pdf）。

---

## 12. 风险与回滚

| 风险 | 影响 | 缓解 |
|---|---|---|
| 仓库无 Java 工具链，构建环境缺 JDK/Maven | 后端无法编译 | 实现阶段先确认环境；若缺，记录为环境依赖缺失，不强行绕过 |
| POI 依赖体积较大 | 打包增大 | 可接受（导出 Excel 必需）；若极端受限可降级 CSV，但需用户确认 |
| 前端静态页面与现有 Hexo 路由冲突 | 路由 404 | `demo.html` 为独立文件，不进 Hexo 路由，无冲突 |
| Git 只读约束 | 无法 commit 设计文档 | 跳过 git commit，仅落盘 .md 文件 |

**回滚**：所有新增均为独立目录（`backend/`）与独立文件（`demo.html`），不改动现有静态站点任何文件；删除新增目录/文件即可完全回滚。

---

## 13. 实现阶段文件清单（供「编码实现」阶段使用，本次不创建）

新增（非修改现有文件）：
1. `backend/pom.xml`
2. `backend/src/main/java/com/example/demo/DemoApplication.java`
3. `backend/src/main/java/com/example/demo/controller/HelloController.java`
4. `backend/src/main/java/com/example/demo/controller/SortController.java`
5. `backend/src/main/java/com/example/demo/controller/ExportController.java`
6. `backend/src/main/java/com/example/demo/service/HelloService.java`
7. `backend/src/main/java/com/example/demo/service/SortService.java`
8. `backend/src/main/java/com/example/demo/service/ExportService.java`
9. `backend/src/main/java/com/example/demo/model/SortResult.java`
10. `backend/src/main/java/com/example/demo/model/R.java`
11. `backend/src/main/java/com/example/demo/handler/GlobalExceptionHandler.java`
12. `backend/src/main/resources/application.yml`
13. `backend/src/main/resources/static/demo.html`（同源托管前端页面）
14. 测试：`backend/src/test/java/com/example/demo/...`（对应 Service/Controller 测试）

不改动：现有 `index.html`、`css/`、`js/`、`lib/`、`page/` 等任何静态站点文件。

---

## 14. 验收检查表（实现阶段完成后核对）

- [ ] `mvn test`（仅 backend 模块）通过。
- [ ] `GET /api/hello` 返回 `Hello, World!`。
- [ ] `POST /api/bubble-sort` 对 `[5,2,8,1,9,3]` 返回 `sorted=[1,2,3,5,8,9]`。
- [ ] `GET /api/export?type=hello` 下载 xlsx 且内容为 `Hello, World!`。
- [ ] `GET /api/export?type=bubble` 下载 xlsx 且内容为排序后数组。
- [ ] `GET /api/export?type=invalid` 返回 400。
- [ ] 前端 `demo.html` 两个 Tab 可切换并正确展示结果。
- [ ] 导出按钮可触发对应 Tab 结果的文件下载。

---

*本文档为需求澄清/系分设计阶段产物，不包含任何代码变更。后续进入「编码实现」阶段时，按 §13 清单落地代码。*
