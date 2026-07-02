# Spring Boot Template

Java 后端项目模板，完成 Maven/Spring Boot
基础骨架、代码风格工具配置、公共后端基础设施、User 示例模块、测试和启动验证。

## 技术栈

- JDK 17
- Maven 3.9.16
- Spring Boot 3.5.16
- MyBatis Spring Boot Starter 3.0.5
- MySQL 8.4.10
- Lombok
- Spotless + Checkstyle

## 公共后端约定

接口统一返回 `ApiResult<T>`：

```json
{
  "code": "200",
  "message": "成功",
  "data": {},
  "traceId": "request-trace-id",
  "timestamp": "2026-07-02T12:00:00"
}
```

分页统一使用 `PageResult<T>`，字段包括 `records`、`total`、`pageNo`、`pageSize`、`pages`。

业务失败时抛出 `BusinessException`，由 `GlobalExceptionHandler` 转为统一响应。参数校验使用
Jakarta Validation 注解，例如 `@NotBlank`、`@NotNull`、`@Min`，Controller 入参使用 `@Valid`
或 `@Validated` 触发校验。

每个请求会通过 `X-Trace-Id` 传递或生成链路 ID，响应头和响应体都会包含同一个 `traceId`，日志
pattern 也会输出 `traceId`，便于排查问题。

## User 示例模块

建表 SQL 位于 `sql/001_create_user_table.sql`，接口统一使用 `/api/v1/users`：

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/v1/users` | 创建用户 |
| `GET` | `/api/v1/users/{id}` | 按 ID 查询用户 |
| `GET` | `/api/v1/users` | 分页查询用户 |
| `PUT` | `/api/v1/users/{id}` | 更新用户 |
| `DELETE` | `/api/v1/users/{id}` | 逻辑删除用户 |

示例：

```powershell
curl -X POST http://localhost:8080/api/v1/users `
  -H "Content-Type: application/json" `
  -d '{"username":"alice","nickname":"Alice","email":"alice@example.com"}'

curl "http://localhost:8080/api/v1/users?pageNo=1&pageSize=10&username=alice"
```

## 质量门禁
```powershell
cd D:\Codes\standard-java-coding\spring-boot-template
.\mvnw.cmd clean verify
```
包含：
- Spotless 
- Checkstyle 
- 编译 
- 单元测试 
- 打包
