# HelloWorld 模块

## 模块职责

提供 HelloWorld 问候语生成与命令行程序入口。

## 关键类

| 类名 | 说明 |
|------|------|
| `HelloWorld` | 主类，包含 `getGreeting()` 静态方法返回问候语，`main()` 方法输出到标准输出 |

## 依赖关系

无外部依赖。

## API 接口

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `getGreeting()` | 无 | `String` | 返回默认问候语 "Hello, World!" |
| `main(String[])` | 启动参数 | `void` | 程序入口，输出问候语到标准输出 |