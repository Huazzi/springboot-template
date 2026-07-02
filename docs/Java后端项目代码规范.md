# Java 后端项目代码规范

> 适用范围：基于 JDK 17、Spring Boot 3.5.x、Maven 3.9.16、MySQL 8.4.10、
> MyBatis、Lombok 的后端项目。

## 1. 规范分级

规范不是为了增加流程负担，而是为了减少协作成本。本文把规则分为三类：

| 等级 | 含义 | 处理方式 |
|------|------|----------|
| 必须遵守 | 影响正确性、安全性、可维护性或团队协作的硬要求 | 进入模板、CI、Code Review 检查 |
| 推荐遵守 | 大多数情况下更好，但允许少量有理由的例外 | Review 时提醒，例外需说明原因 |
| 团队约定 | 当前团队为了统一风格做出的选择 | 新项目默认采用，后续可版本化调整 |

核心原则：

- 必须遵守：规范即代码，能自动检查的不要只靠人工约定。
- 必须遵守：模板必须可运行，不能只是空架子。
- 必须遵守：优先清晰、稳定、可测试，避免过度分层。
- 团队约定：本模板只使用 `Entity`、`Request`、`Response` 等必要概念，不默认引入 DTO、BO、VO、PO 的复杂命名体系。

## 2. Git 分支规范

为什么：分支命名统一后，团队可以从分支名判断工作性质、关联需求和发布风险。

必须遵守：

- `main`：生产可发布分支，只接受已验证代码。
- `develop`：日常集成分支，承载下一次发布内容。
- `feature/<issue>-<short-desc>`：功能开发，例如 `feature/123-user-page`。
- `fix/<issue>-<short-desc>`：缺陷修复，例如 `fix/245-user-delete`。
- `hotfix/<issue>-<short-desc>`：线上紧急修复。
- `release/<version>`：发布准备分支，例如 `release/1.2.0`。

推荐遵守：

- 分支名使用小写字母、数字和短横线。
- 一个分支只做一类变更，避免功能、重构、格式化混在一起。

团队约定：

- 合并到 `main` 或 `develop` 前必须通过 `mvnw.cmd clean verify`。
- 大范围格式化应单独提交，避免掩盖真实业务改动。

## 3. Git 提交规范

为什么：提交信息是长期维护时的线索。采用 Conventional Commits 可以自动生成变更日志，也方便快速定位风险。

必须遵守：

```text
<type>(<scope>): <subject>
```

常用 `type`：

| type | 说明 |
|------|------|
| `feat` | 新功能 |
| `fix` | 缺陷修复 |
| `docs` | 文档 |
| `style` | 仅格式调整 |
| `refactor` | 重构，不改变外部行为 |
| `test` | 测试 |
| `chore` | 构建、依赖、脚手架等杂项 |

示例：

```text
feat(user): 添加用户查询页面 API
fix(user): 使用逻辑删除来移除用户
docs: 添加后端代码规范
test(user): 创建用户验证
```

推荐遵守：

- `subject` 使用祈使句或简短动宾短语。
- 一次提交表达一个完整意图。

团队约定：

- 破坏性变更在正文中写 `BREAKING CHANGE:` 并说明迁移方式。

## 4. Maven 项目结构规范

为什么：结构稳定后，新成员能快速找到入口、配置、SQL、测试与文档，减少“靠问人”的隐性成本。

必须遵守：

```text
spring-boot-template/
  pom.xml
  mvnw
  mvnw.cmd
  checkstyle.xml
  README.md
  src/main/java/com/chaoxing/template
  src/main/resources
  src/main/resources/mapper
  src/test/java/com/chaoxing/template
  sql
```

必须遵守：

- Java 版本统一为约定版本。
- Maven Wrapper 固定 Maven 版本，团队优先使用 `mvnw.cmd` 或 `./mvnw`。
- 依赖版本优先交给 Spring Boot parent/BOM 管理，不随意手写版本。
- MyBatis Starter 使用与 Spring Boot 兼容的版本线。

推荐遵守：

- 业务 SQL 初始化文件放在 `sql/`，命名使用序号前缀，例如 `001_create_user_table.sql`。
- 配置文件按环境拆分为 `application-local.yml`、`application-dev.yml`、`application-prod.yml`。

## 5. Java 包结构规范

为什么：包结构是代码的第一层导航。好的包结构应该让新人不用搜索全项目，也能判断一个类属于哪个业务、承担什么职责。

必须遵守：

- 根包使用反向域名规则，格式建议为 `com.<公司名或组织名>.<项目名>`，例如：
  - `com.example.order`
  - `com.chaoxing.crm`
  - `cn.company.platform`
- 根包必须稳定，项目创建后不要频繁修改，否则会造成大量包名和导入变更。
- 公共能力放在根包下的 `common` 或 `shared` 中，例如：
  - `common.response`
  - `common.exception`
  - `common.web`
- 不同业务模块之间不要互相直接访问对方的 Mapper 或 Entity，跨模块协作应优先通过 Service 或明确的应用接口完成。

推荐遵守：

- 小项目或业务边界清晰的项目，优先采用“按业务领域聚合”的包结构：

```text
com.<公司名>.<项目名>
  common
  user
    controller
    service
    service.impl
    mapper
    entity
    request
    response
  order
    controller
    service
    service.impl
    mapper
    entity
    request
    response
```

- 简单后台、教学示例或团队已有强约定时，也可以采用“按技术分层”的包结构：

```text
com.<公司名>.<项目名>
  controller
    UserController
    OrderController
  service
    UserService
    OrderService
  service.impl
    UserServiceImpl
    OrderServiceImpl
  mapper
    UserMapper
    OrderMapper
  entity
    UserEntity
    OrderEntity
  request
  response
```

- 当业务模块逐渐变多时，推荐从“按技术分层”演进为“按业务领域聚合”。原因是后者把同一个业务的 Controller、Service、Mapper、Entity、Request、Response 放得更近，改一个业务时更容易定位相关代码，也更容易控制模块边界。
- 不要过早拆多模块 Maven。只有当代码规模、发布节奏或依赖边界真的需要独立时，再考虑 Maven 多模块。
- 只有当公共代码被两个以上模块复用时，再沉淀到 `common`。

团队约定：

- 本模板采用“按业务领域聚合”，因为它更适合作为长期演进的业务项目起点。
- 如果团队已有成熟的“按技术分层”习惯，可以继续使用，但要避免一个包下文件过多导致难以维护。

## 6. Controller、Service、Mapper 分层规范

为什么：分层的目标是职责清楚。

必须遵守：

- Controller 只负责 HTTP 入参、校验触发、调用 Service、返回统一结果。
- Service 负责业务规则、事务边界、业务异常、对象转换。
- Mapper 只负责数据库访问，不写业务判断。
- Mapper XML 只写 SQL 和必要动态条件，不承载复杂业务流程。

推荐遵守：

- Controller 方法保持短小，避免在 Controller 中拼 SQL、算复杂业务状态。
- Service 中的私有方法用于表达业务步骤，避免把一个方法写成一整屏流水账。

团队约定：

- 查询不存在的数据时，Service 抛出 `BusinessException(ErrorCode.NOT_FOUND, "...")`。
- 删除默认逻辑删除，除非业务明确要求物理删除并经过评审。

## 7. Entity、Request、Response 使用规范

为什么：对象边界清楚，可以避免数据库字段直接泄漏到 API，也能让入参与出参独立演进。

必须遵守：

- `Entity` 对应数据库表结构，不直接作为接口入参或响应。
- `Request` 只表达接口入参，并承载 Jakarta Validation 注解。
- `Response` 只表达接口出参，不暴露 `deleted` 等内部字段。
- 创建、更新、查询必须使用不同 Request，例如：
  - `UserCreateRequest`
  - `UserUpdateRequest`
  - `UserQueryRequest`

推荐遵守：

- `Response` 可以提供 `from(Entity entity)` 这种简单转换方法。
- 大量复杂映射再考虑引入 MapStruct 等工具，不要一开始就增加依赖。

团队约定：

- 字段命名使用 Java camelCase，数据库使用 snake_case，由 MyBatis 映射。

## 8. RESTful API 设计规范

为什么：API 风格统一后，调用方不需要猜接口语义，也更容易写网关、文档和测试。

必须遵守：

- API 使用版本前缀：`/api/v1`。
- 资源使用名词复数：`/api/v1/users`。
- HTTP 方法表达动作：
  - `POST /api/v1/users` 创建
  - `GET /api/v1/users/{id}` 查询详情
  - `GET /api/v1/users` 分页查询
  - `PUT /api/v1/users/{id}` 更新
  - `DELETE /api/v1/users/{id}` 删除
- 路径中使用资源标识，查询条件放 query string。

推荐遵守：

- 列表接口默认分页，避免一次返回过多数据。
- 分页参数统一使用 `pageNo` 和 `pageSize`。
- `pageSize` 应设置上限，本模板约定最大 100。

团队约定：

- 删除接口语义是逻辑删除，响应仍使用统一返回结果。

## 9. 统一返回结果规范

为什么：统一响应可以降低前后端对接成本，也能把 traceId、错误码、分页结构固化下来。

必须遵守：

成功响应统一使用 `ApiResult<T>`：

```json
{
  "code": "00000",
  "message": "成功",
  "data": {},
  "traceId": "request-trace-id",
  "timestamp": "2026-07-02T12:00:00"
}
```

分页响应统一使用 `PageResult<T>`：

```json
{
  "records": [],
  "total": 0,
  "pageNo": 1,
  "pageSize": 10,
  "pages": 0
}
```

必须遵守：

- Controller 不直接返回裸对象。
- 分页接口返回 `ApiResult<PageResult<Response>>`。
- 错误响应也包含 `traceId`，便于排查。

推荐遵守：

- `code` 面向程序判断，`message` 面向用户或调用方理解。
- 不要用 HTTP 200 包装所有错误；业务异常、参数错误、404、500 应使用合理 HTTP 状态。

团队约定：

- 成功码为 `00000`。
- 参数错误使用 `A0400`，业务失败使用 `B0001`，系统异常使用 `B0500`。

## 10. 全局异常处理规范

为什么：异常处理分散在 Controller 会导致响应格式不一致，也容易泄露内部堆栈。

必须遵守：

- 业务异常使用 `BusinessException`。
- 错误码统一放在 `ErrorCode`。
- 使用 `GlobalExceptionHandler` 统一处理：
  - `BusinessException`
  - 参数校验异常
  - 请求体解析异常
  - HTTP method/media type 不支持
  - 资源不存在
  - 未预期异常
- 未预期异常只返回通用系统错误，不把堆栈返回给客户端。

推荐遵守：

- 业务异常日志使用 `warn`。
- 未预期异常日志使用 `error` 并打印异常对象。

团队约定：

- “未找到”使用 `ErrorCode.NOT_FOUND`，例如用户不存在。
- 参数为空、格式不合法、范围不合法使用 `ErrorCode.PARAM_INVALID`。

## 11. 参数校验规范

为什么：越靠近入口校验，越早失败，业务代码越干净。

必须遵守：

- 请求对象使用 Jakarta Validation 注解。
- `@RequestBody` 入参使用 `@Valid`。
- `@PathVariable`、`@RequestParam` 校验时 Controller 类加 `@Validated`。
- 参数错误由全局异常处理器转换为统一响应。

示例：

```java
@PostMapping
public ApiResult<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
  return ApiResult.success(userService.create(request));
}
```

推荐遵守：

- 校验消息应短小明确，例如“用户名不能为空”。
- 复杂业务校验放在 Service，例如“用户名已存在”。

团队约定：

- 列表分页参数默认 `pageNo = 1`、`pageSize = 10`。
- `pageSize` 最大值为 100。

## 12. 日志规范

为什么：日志是线上排查问题的主要证据。日志缺上下文，再多也很难用。

必须遵守：

- 每个请求必须有 `traceId`。
- 响应头使用 `X-Trace-Id`。
- 日志 pattern 输出 MDC 中的 `traceId`。
- 禁止在日志中输出密码、token、身份证号等敏感数据。

推荐遵守：

- 正常业务流程不滥用 `info`。
- 可预期业务失败使用 `warn`。
- 系统异常使用 `error` 并带异常堆栈。
- SQL 调试日志只在 local/dev 环境开启。

团队约定：

- 模板通过 `TraceIdFilter` 生成或透传 `X-Trace-Id`。
- 本地默认 `com.chaoxing.template` 为 `debug`，生产环境应收敛到 `info` 或更高。

## 13. MyBatis XML 与 Mapper 规范

为什么：SQL 是后端系统最容易积累复杂度的地方，必须让查询条件、字段映射和逻辑删除保持可读。

必须遵守：

- Mapper 接口加 `@Mapper`。
- Mapper XML namespace 与 Mapper 接口全限定名一致。
- 参数超过一个时使用 `@Param` 命名。
- SQL 参数使用 `#{}`，禁止使用 `${}` 拼接用户输入。
- 默认查询必须过滤逻辑删除字段。
- XML 中维护 `resultMap`，明确列到字段的映射。

推荐遵守：

- 公共列使用 `<sql id="BaseColumns">` 复用。
- 动态查询条件使用 `<if>`，保持条件简单。
- 复杂查询优先拆方法，不要把多个业务场景塞进同一条 SQL。

团队约定：

- Mapper XML 放在 `src/main/resources/mapper/<module>/`。
- 表字段使用 snake_case，Java 属性使用 camelCase。

## 14. 数据库字段设计建议

为什么：数据库字段一旦上线，调整成本高，应在模板阶段建立稳定默认值。

必须遵守：

- 表必须有主键 `id BIGINT`。
- 业务表必须有 `created_at`、`updated_at`。
- 需要逻辑删除的表使用 `deleted` 字段。
- 字符集使用 `utf8mb4`。
- 重要唯一约束必须由数据库保证，例如 `username` 唯一。

推荐遵守：

- 时间字段使用 `DATETIME(3)`，便于保留毫秒。
- 状态字段使用清晰注释，例如 `0禁用，1启用`。
- 高频查询条件建立索引，但避免盲目加索引。
- 字段注释要表达业务含义，不要只重复字段名。

团队约定：

- 逻辑删除字段：`deleted TINYINT(1) NOT NULL DEFAULT 0`。
- 创建时间字段：`created_at`。
- 更新时间字段：`updated_at`。

## 15. 事务规范

为什么：事务边界不清楚会导致部分成功、部分失败，排查和补偿都困难。

必须遵守：

- 事务写在 Service 层，不写在 Controller 或 Mapper。
- 涉及多条写操作的方法必须加 `@Transactional(rollbackFor = Exception.class)`。
- 事务方法应为 `public`，避免 Spring AOP 不生效。
- 不在事务中执行耗时外部调用，例如远程 HTTP、消息阻塞等待。

推荐遵守：

- 只读查询不加事务，除非有一致性要求。
- 单表单语句写入可不强制加事务，但模板中允许统一加在写 Service 上。

团队约定：

- 创建、更新、删除接口对应 Service 方法默认加事务。

## 16. 多环境配置规范

为什么：环境隔离可以避免本地配置污染生产，也能让部署流程更稳定。

必须遵守：

- 公共配置放 `application.yml`。
- 本地配置放 `application-local.yml`。
- 开发环境配置放 `application-dev.yml`。
- 生产环境配置放 `application-prod.yml`。
- 密码、token、密钥不得提交到仓库，使用环境变量注入。

推荐遵守：

- 默认 profile 为 `local`。
- 数据库连接用 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 覆盖。
- 生产环境关闭或收敛 debug 日志。

团队约定：

- 本模板保留 MySQL 驱动、MyBatis mapper 路径、Jackson 时区等公共配置。

## 17. 测试规范

为什么：模板中的测试不是装饰品，而是告诉团队“什么行为不能被改坏”。

必须遵守：

- 新增公共基础设施必须有单元测试或 Web 层测试。
- 新增业务模块至少覆盖 Controller 和 Service。
- 测试命名应表达行为，例如 `shouldCreateUser`。
- 提交前运行 `mvnw.cmd test` 或 `./mvnw test`。

推荐遵守：

- Controller 测试关注路由、校验、统一响应结构。
- Service 测试关注业务规则、异常、分页、事务前后的判断。
- Mapper SQL 后续可使用集成测试或 Testcontainers 覆盖。

团队约定：

- 测试类放在与生产代码同包路径的 `src/test/java` 下。
- MockMvc 测试可使用 standalone setup，让 Web 层测试更轻。

## 18. 基础安全规范

为什么：即使当前不引入 Spring Security，也不能忽视最基本的输入、输出和配置安全。

必须遵守：

- 所有外部输入必须校验。
- SQL 必须使用预编译参数 `#{}`。
- 响应中不返回堆栈、SQL、内部路径等敏感信息。
- 日志中不得打印密码、token、密钥和完整个人敏感信息。
- 生产配置不得提交真实账号密码。

推荐遵守：

- 后续引入登录能力时，密码必须使用强哈希算法，不可明文或可逆加密存储。
- 对批量查询、分页大小、导出接口设置上限。
- 对管理类接口增加鉴权和审计。
- 定期检查依赖漏洞。

团队约定：

- 当前模板暂不使用 Spring Security，但保留清晰的 API、异常、日志和校验边界，便于后续接入安全框架。

## 19. 代码格式化和静态检查规范

为什么：格式争论最适合交给工具。人的注意力应留给业务正确性、边界条件和设计风险。

必须遵守：

- 使用 `.editorconfig` 固化编码、缩进、换行。
- 使用 Spotless 自动格式化 Java、XML、YAML、Markdown 等文件。
- 使用 Checkstyle 做静态规则检查。
- `validate` 阶段必须执行 Spotless check 和 Checkstyle。
- 提交前至少运行：

```powershell
.\mvnw.cmd spotless:apply
.\mvnw.cmd test
```

推荐遵守：

- IDE 启用 `.editorconfig`。
- 不在业务提交里混入无关格式化。
- 规则冲突时优先让格式器和静态检查达成一致，而不是让开发者手动猜格式。

团队约定：

- Java 使用 google-java-format。
- 行宽上限为 100。
- 使用 LF 换行，避免 Windows CRLF 与 CI 不一致。

## 20. 团队落地建议

为什么：规范真正生效，靠的是模板、工具、评审和持续维护，而不是一次性发文。

必须遵守：

- 新项目从模板创建，不从零手写基础设施。
- PR 必须通过自动检查。
- Code Review 优先看正确性、边界条件、事务、安全和测试，不把时间浪费在可自动修复的格式问题上。

推荐遵守：

- 每次规范调整都同步更新模板、README 和本文档。
- 团队每隔一段时间回顾一次规范，删除无效规则，补齐高频问题。
- 对历史项目分批迁移，不要求一次性大爆炸式改造。

团队约定：

- 规范例外必须写明原因，例如性能、兼容性、遗留系统约束。
- 模板中的 User 模块作为新增业务模块的参考样例。

## 21. 参考资料

- Spring Boot 官方项目页：https://spring.io/projects/spring-boot
- Spring Boot 官方文档：https://docs.spring.io/spring-boot/
- Spring Boot 3.5 系统要求：https://docs.spring.io/spring-boot/3.5/system-requirements.html
- Google Java Style Guide：https://google.github.io/styleguide/javaguide.html
- Conventional Commits：https://www.conventionalcommits.org/en/v1.0.0/
- MyBatis Spring Boot Starter：https://github.com/mybatis/spring-boot-starter
- Maven Wrapper 文档：https://maven.apache.org/tools/wrapper/
- Maven Checkstyle Plugin：https://maven.apache.org/plugins/maven-checkstyle-plugin/
- Spotless Maven Plugin：https://github.com/diffplug/spotless/tree/main/plugin-maven
- Alibaba Java Coding Guidelines / P3C：https://github.com/alibaba/p3c
