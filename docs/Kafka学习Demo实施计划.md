# Kafka 学习 Demo 实施计划

> 本文档是 springboot-template 项目中新增 **Kafka 学习 Demo** 的实施计划，由开发者自行按步骤执行。
> 相关仓库约定：JDK 17 / Spring Boot 3.5.16 / Maven 3.9.16、spotless(google-java-format) + checkstyle 绑在 `validate` 阶段、全中文注释、业务领域分包（`com.chaoxing.template` 下的 `user` / `common` / `kafka`）。

## 一、背景与目标

正在学习消息队列，希望在这个 Spring Boot 学习模板中加入一个 Kafka 应用 Demo，通过动手实践加深对 MQ 的理解。项目当前已集成 MySQL + Redis（纯 starter 自动配置 + yml 模式，无自定义 `@Configuration`），无 Kafka 依赖、无 docker-compose。

目标：既有**独立的 Kafka 演示模块**（REST 端点自由实验核心概念），又有**真实的业务落地示例**（用户创建时发布事件、异步消费），让学习既覆盖概念又看到实际价值。

### 已确定的决策

| 决策点 | 选择 | 理由 |
|---|---|---|
| Demo 形式 | 独立演示模块 + user 模块集成 | 学习最完整 |
| Kafka 启动方式 | 新增根目录 `docker-compose.yml`（单节点 KRaft） | 一条命令拉起，本地学习最省心 |
| 概念深度 | 进阶全套 | 多消费者组、key/分区/顺序、手动 ack、重试 + 死信、幂等消费 |

## 二、设计原则（复用现有约定）

- 沿用项目「业务领域分包」模式：新增 `com.chaoxing.template.kafka` 包，内含 `config / dto / producer / consumer / controller` 子包。
- 沿用「starter 自动配置 + yml」的中间件集成风格：默认消费者工厂用 Boot 自动配置，只在需要差异处补自定义工厂（这是对现有 Redis「零配置类」先例的可控例外，且本身就是教学点）。
- 全部中文注释，遵守 spotless google-java-format（2 空格缩进、100 列）+ checkstyle。
- REST 统一返回 `Result<T>`、构造器注入（`@RequiredArgsConstructor`）、Service 抛 `ServiceException`。

## 三、技术关键点（已核实，避免踩坑）

1. **版本**：spring-kafka 由 Boot 3.5.16 BOM 管理，实际解析为 **3.3.16**（kafka-clients 3.9.2）——依赖一律交 BOM，不手写版本。
2. **Topic 创建**：Boot 自动注册 `KafkaAdmin`，只需声明 `NewTopic` Bean（`TopicBuilder`）启动时自动建 topic；`fail-fast=false` 时 broker 未就绪只记日志、不阻断启动。
3. **手动 ack**：`@KafkaListener(ackMode=...)` 是 spring-kafka **4.1+** 才有的属性；本项目 3.3.16 **必须**用自定义 `ConcurrentKafkaListenerContainerFactory` 设 `AckMode.MANUAL_IMMEDIATE`，监听方法带 `Acknowledgment` 参数手动 `acknowledge()`/`nack()`。
4. **反序列化**：`JsonDeserializer` 需配 `spring.json.trusted.packages` 白名单放行应用包；类型由 `JsonSerializer` 写入的 `__TypeId__` 头决定。
5. **序列化器 ObjectMapper 是自带的裸实例**（无 JavaTimeModule、无 non_null）——**DTO 时间字段一律用 ISO `String`，不放 `LocalDateTime`**。
6. **`@RetryableTopic`**：默认后缀 `-retry`/`-dlt`；指数退避按延迟值命名（`-retry-1000`、`-retry-2000`），retry/DLT 自动继承主主题分区数；重试期间不占消费线程（非阻塞）。
7. **幂等消费**：复用已有 Redis，`setIfAbsent(dedupKey, "1", TTL)` 原子去重；注释写明「claim 后处理」取舍（崩溃会丢，生产常用唯一约束/outbox）。
8. **`@EmbeddedKafka` 测试**：spring-kafka-test 3.3.x 内嵌 broker 以 KRaft 自举，用 `@SpringJUnitConfig` + 静态 `@Configuration @EnableKafka`（**不要用 `@SpringBootTest`**，否则会连带拉起 MySQL/Redis 数据源）。

## 四、实施步骤

### 1. 基础设施：`docker-compose.yml`（项目根目录，新增）

> 该文件若尚不存在则新建。单节点 KRaft Kafka（无 Zookeeper），官方 `apache/kafka` 镜像（tag 对齐 kafka-clients 3.9.2；Bitnami 免费镜像已下架不采用）。

```yaml
services:
  # 单节点 Kafka，用于本地学习。采用 KRaft 模式（无需 Zookeeper）。
  # 启动：docker compose up -d；验证：docker compose logs kafka
  kafka:
    image: apache/kafka:3.9.2
    container_name: springboot-template-kafka
    ports:
      - "9092:9092"
    environment:
      KAFKA_NODE_ID: 1
      # 单节点同时承担 broker 与 controller 两种角色
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka:9093
      KAFKA_LISTENERS: PLAINTEXT://:9092,CONTROLLER://:9093
      # 宿主机中的应用通过 localhost:9092 连接
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
      # 任意合法的 base64 编码 16 字节，用于首次启动时格式化存储目录
      KAFKA_CLUSTER_ID: MkU3OEVBNTcwNTJENDM2Qk
      # 单节点集群下副本数必须为 1，否则内部主题创建会失败
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
      # 日志目录用镜像默认路径，位于容器可写层（appuser 可写）。
      # 注意：不挂载数据卷——官方 apache/kafka 镜像以非 root 用户(appuser/uid 1000)运行，
      # 命名卷初始化为 root 属主（755）会导致格式化元数据时 AccessDeniedException。
      # 学习场景下数据不持久化：docker compose stop/start 数据保留，down 后清空。
      KAFKA_LOG_DIRS: /tmp/kraft-combined-logs
```

### 2. `pom.xml`（修改）

`<dependencies>` 追加（版本由 BOM 管理，不写 `<version>`）：

- `org.springframework.kafka:spring-kafka`
- `org.springframework.kafka:spring-kafka-test`（`<scope>test</scope>`）

### 3. 配置：`src/main/resources/application.yml`（修改）

在 `spring:` 下追加（放公共配置，profile 文件无需改动；地址走环境变量覆盖，与现有 `${DB_URL:...}` 风格一致）：

```yaml
  kafka:
    bootstrap-servers: "${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}"
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all
      retries: 3
      properties:
        enable.idempotence: true
        linger.ms: 5
    consumer:
      group-id: template-default-group
      enable-auto-commit: false
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: com.chaoxing.template
        spring.json.value.default.type: java.lang.String
    listener:
      ack-mode: RECORD
    admin:
      fail-fast: false
```

`auto-offset-reset: earliest` 是学习场景关键（否则"先生产后消费"看不到历史消息）。

### 4. Kafka 模块（`com.chaoxing.template.kafka`，新增 12 个主类）

**`config/KafkaConfig.java`**（项目唯一自建 `@Configuration`，注释说明例外）：
- 5 个 `NewTopic` Bean（`TopicBuilder`，RF 均 1）：`demo.plain`(1p)、`demo.keyed`(2p，组内负载均衡演示)、`demo.manualack`(1p)、`demo.retry`(1p)、`user.created`(2p)。`demo.retry` 的 retry/DLT 主题由 `@RetryableTopic` 自动建，**不要再写 NewTopic**。
- `manualAckKafkaListenerContainerFactory`：复用 Boot 的 `ConsumerFactory`，`setAckMode(MANUAL_IMMEDIATE)`。
- `keyedOrderingContainerFactory`：`setConcurrency(2)`（2 分区 + 2 消费线程 = 组内负载均衡）。
- 泛型坑：注入 `ConsumerFactory<Object,Object>`，实例化 `ConcurrentKafkaListenerContainerFactory<Object,Object>`；监听方法具体类型由 MessageConverter 转换，与工厂泛型无关。

**`dto/DemoMessage.java`** — `Long id`、`String content`、`String timestamp`（ISO 字符串，原因见坑 2）；`@Getter @Setter` + 默认构造器 + `of(content)` 静态工厂。
**`dto/UserCreatedEvent.java`** — `String eventId`(UUID，幂等去重键)、`Long id`、`String username`、`String nickname`、`String email`、`String createdAt`(ISO)；静态工厂 `from(UserResponse)`。
**`dto/KafkaMessageRequest.java`** — `@NotBlank @Size(max=256)` content，中文提示。

**`producer/KafkaDemoProducer.java`** — 注入 Boot 的 `KafkaTemplate<String,Object>`；`sendPlain`/`sendKeyed(key,...)`/`sendManualAck`/`sendRetry`；统一私有 `send(topic, key, value)` 挂 `whenComplete` 记 topic/partition/offset 或 error（序列化异步，异常冒到 future，必须看回调）。

**`producer/KafkaEventPublisher.java`** — 复刻 `UserServiceImpl.putCache` 的 afterCommit 模式（commit 9af0601）：

```java
public void publish(UserCreatedEvent event) {
  if (TransactionSynchronizationManager.isSynchronizationActive()) {
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            doPublish(event);
          }
        });
  } else {
    doPublish(event);
  }
}
```

key=userId → 同用户事件同分区保序。类注释写明：回滚则不发布；Kafka 写失败不回滚 DB（会丢事件），生产级方案是事务性发件箱(outbox)，仅注释说明不实现。

**`consumer/PlainMessageConsumer.java`** — `demo.plain`，两个 `@KafkaListener`，groupId=`demo-group-a`/`demo-group-b` → **跨组广播**（两组各自收到全部消息），日志打 `[广播] 消费组 xxx 收到...`。

**`consumer/KeyedMessageConsumer.java`** — `demo.keyed`，`containerFactory="keyedOrderingContainerFactory"`，方法签名含 `@Header(RECEIVED_KEY)`/`RECEIVED_PARTITION`/`OFFSET` → **按 key 有序 + 组内负载均衡**（同 key 恒同分区同线程、offset 严格递增）。

**`consumer/ManualAckConsumer.java`** — `demo.manualack`，`containerFactory="manualAckKafkaListenerContainerFactory"`，方法 `(DemoMessage, Acknowledgment)`：content=="fail" 则 `ack.nack(Duration.ofSeconds(1))`（不提交位移→延迟重投），成功 `acknowledge()`。注释强调 Acknowledgment 只在 MANUAL/MANUAL_IMMEDIATE 工厂下非 null。

**`consumer/RetryableMessageConsumer.java`** — `demo.retry`，`@RetryableTopic(attempts="3", backoff=@Backoff(delay=1000, multiplier=2), autoCreateTopics="true")` + `@KafkaListener` + `@DltHandler`；content=="fail" 抛异常 → 重试 → DLT。手动 ack 与重试**刻意分到两个主题**，避免二者叠加的位移/提交边界交互干扰演示。

**`consumer/UserCreatedEventConsumer.java`** — `user.created`，独立 groupId；`setIfAbsent("kafka:dedup:user:created:" + eventId, "1", Duration.ofMinutes(10))` 判重（false 即 `[幂等] 跳过`），首次打 `[用户事件] 开始处理...`（注释给出业务落点：欢迎邮件/初始化/同步索引）。

**`controller/KafkaDemoController.java`** — `@Validated @RestController @RequestMapping("/api/v1/kafka")`，注入 `KafkaDemoProducer`，返回 `Result<Void>`：

| 端点 | 演示点 |
|---|---|
| `POST /api/v1/kafka/plain`（body content） | 跨组广播（两组全收） |
| `POST /api/v1/kafka/plain/batch?count=N`（`@Min(1) @Max(100)`） | 连发 N 条观察广播 |
| `POST /api/v1/kafka/keyed?key=user-1` | key 分区路由 |
| `POST /api/v1/kafka/keyed/batch?key=user-1&count=N` | 同 key 连发 → 同分区 offset 递增有序 |
| `POST /api/v1/kafka/manualack` | 手动 ack；content=fail 演示重投 |
| `POST /api/v1/kafka/retry` | content=fail 触发 3 次重试后进 DLT |

### 5. user 模块集成（改 1 主类 + 1 测试）

**`UserServiceImpl.java`** — 加字段 `private final KafkaEventPublisher kafkaEventPublisher;`；`create()` 尾部把 `return getById(entity.getId())` 改为：

```java
UserResponse response = getById(entity.getId());
kafkaEventPublisher.publish(UserCreatedEvent.from(response));
return response;
```

（create 是 `@Transactional`，publish 走 afterCommit，回滚时不发事件。）

**`UserServiceImplTest.java`** — **必须**加 `@Mock KafkaEventPublisher`，否则 `@InjectMocks` 注入 null → create 测试 NPE；并加 `verify(kafkaEventPublisher).publish(any(UserCreatedEvent.class))`。`UserControllerTest` 不受影响。

### 6. 测试（新增，沿用现有风格）

- `kafka/producer/KafkaEventPublisherTest.java` — `@Mock KafkaTemplate<String,Object>`；无事务分支 verify send；有事务分支 `initSynchronization()`+`setActualTransactionActive(true)` 后 `verifyNoInteractions`，`@AfterEach clearSynchronization()`（无 Spring 上下文无法触发 afterCommit，单测只断言"推迟登记"）。
- `kafka/producer/KafkaDemoProducerTest.java` — mock KafkaTemplate，校验各 send 调用参数。
- `kafka/controller/KafkaDemoControllerTest.java` — `standaloneSetup` + mock producer + 真实 `GlobalExceptionHandler`/`TraceIdFilter`/`LocalValidatorFactoryBean`；校验成功路径与 content 空 → A0400。
- `kafka/KafkaRoundTripTest.java` — `@SpringJUnitConfig(Config)` + `@DirtiesContext` + `@EmbeddedKafka(partitions=2, topics={"demo.roundtrip"})`，静态 `@Configuration @EnableKafka` 定义 ProducerFactory(Json)/KafkaTemplate/ConsumerFactory(String/Json + trustedPackages("*"))/ContainerFactory(RECORD)/测试监听器；用例：String 与 DTO 往返 + 同 key 连发 10 条断言 partition 恒定、顺序一致。控制 2~3 个用例避免拖慢"秒回"单测。
- 不用 `@SpringBootTest`（会拉 MySQL/Redis，违反项目测试现状）。

### 7. `README.md`（修改）

新增「Kafka 学习 Demo」章节：compose 启动、`spring.kafka` 配置表、6 个端点表、四个概念讲解（广播 vs 负载均衡 / key-分区-顺序 / 手动 ack + 非阻塞重试 + DLT / Redis 幂等）、afterCommit 发布 + outbox 说明、验证命令与预期日志、常见坑清单。

## 五、环境准备：Docker Desktop 启动 Kafka

> 本节是可独立执行的运维步骤，先于应用启动完成。前提：已安装并打开 Docker Desktop（启动后需等引擎就绪，首次可能需几分钟）。

### 1. 就绪自检

```bash
docker info            # 出现 Server 段、无 "Cannot connect" 即引擎就绪
docker compose version # 应显示 Docker Compose version v2+
```

引擎未就绪时 `docker info` 报 `Cannot connect to the Docker daemon`——打开 Docker Desktop，等右上角鲸鱼图标变绿。

### 2. 首次启动（在项目根目录执行）

```bash
cd /Users/zzhua/Projects/personal/springboot-template
docker compose up -d   # 首次会拉取 apache/kafka:3.9.2 镜像（约几百 MB），视网速等数分钟
```

### 3. 验证就绪

```bash
docker compose ps                    # STATUS 应为 Up，PORTS 显示 0.0.0.0:9092->9092/tcp
docker compose logs kafka --tail 20  # 出现 "Kafka Server started" 即就绪（约 10~30 秒）
```

### 4. 日常运维

| 目的 | 命令 |
|---|---|
| 停止（保留数据） | `docker compose stop` |
| 再次启动 | `docker compose start` |
| 跟踪日志 | `docker compose logs -f kafka` |
| 停止并删除容器（保留数据） | `docker compose down` |
| 停止并删除容器 + 数据（清空） | `docker compose down -v` |

> 当前 compose 未挂数据卷（见第一节注释），`down` 后数据即清空；`stop/start` 数据仍在容器可写层。

### 5. 排错

- **端口 9092 被占用**：`docker compose logs kafka` 报 `Address already in use` → 查占用并释放，或改 `docker-compose.yml` 的 `ports`。
- **镜像拉取超时/失败**：重试 `docker compose pull kafka`；检查网络与代理。
- **容器秒退 / AccessDeniedException**：不要给日志目录挂数据卷（Kafka 镜像以非 root 用户运行，命名卷属主 root 会无写权限）；Kafka 用容器内默认目录 `KAFKA_LOG_DIRS: /tmp/kraft-combined-logs` 即可。
- **应用连不上**：确认 `docker compose ps` 为 Up 且日志已出现 `Kafka Server started`；本机应用连 `localhost:9092`。

### 6. 与应用配合的启动顺序

```bash
docker compose up -d     # 1. 先起 Kafka（KafkaAdmin 用正确分区数建主题）
./mvnw spring-boot:run   # 2. 再启动应用（需本地 MySQL/Redis）
```

## 六、验证方式（端到端）

```bash
docker compose up -d                     # 先起 Kafka（KafkaAdmin 用正确分区数建主题）
./mvnw clean verify                      # 门禁：spotless + checkstyle + 单测
./mvnw spring-boot:run                   # 启动应用（需本地 MySQL/Redis）
```

1. **广播**：`curl -X POST localhost:8080/api/v1/kafka/plain -H 'Content-Type: application/json' -d '{"content":"hello"}'` → `demo-group-a` 与 `demo-group-b` 各打印一条（跨组广播）。
2. **顺序 + 负载均衡**：`curl -X POST 'localhost:8080/api/v1/kafka/keyed/batch?key=user-1&count=10'` → 同 key 日志 `partition=` 恒定、`offset=` 严格递增有序。
3. **手动 ack 重投**：content=fail → 先 warn「处理失败，重新投递」，1 秒后同条再次出现。
4. **重试 + 死信**：`curl -X POST localhost:8080/api/v1/kafka/retry -H 'Content-Type: application/json' -d '{"content":"fail"}'` → 首次消费 → `-retry-1000` → `-retry-2000` → 最终 `[死信]` 落 `demo.retry-dlt`。
5. **user 集成**：`curl -X POST localhost:8080/api/v1/users -H 'Content-Type: application/json' -d '{"username":"kafka_user_1","nickname":"Kafka","email":"k@e.com"}'` → 控制台出现 `[用户事件] 开始处理...`。
6. **格式**：编码后先 `./mvnw spotless:apply`。

## 七、必须点名的坑（写入注释/README）

1. JsonDeserializer 必须配 `trusted.packages`，否则反序列化直接抛 `SerializationException`。
2. JsonSerializer 自带裸 ObjectMapper（无 JavaTimeModule）→ DTO 时间用 ISO String。
3. `@RetryableTopic` 按延迟值命名主题（`-retry-1000`/`-retry-2000`）、自动继承分区数；勿手动建 retry/DLT 主题。
4. `enable-auto-commit: false` + ack-mode 配合；手动 ack 只对 MANUAL/MANUAL_IMMEDIATE 工厂生效，Acknowledgment 其他模式下为 null。
5. 启动顺序：**先 `docker compose up -d` 再启动应用**，否则 broker 兜底只建 1 分区，`demo.keyed` 演示失真。
6. console producer 发裸字符串会因 JsonDeserializer 解析失败，需发 JSON 引号形式。
7. 新代码过 spotless/checkstyle（100 列、禁通配 import、中文注释、LF、2 空格）。
8. `UserServiceImplTest` 必须补 `@Mock KafkaEventPublisher`。
9. 自定义工厂泛型用 `Object,Object`，监听方法具体类型由 MessageConverter 转换。
10. spring-kafka 实际版本是 **3.3.16**（非 3.5.x），版本一律交 BOM。
11. `apache/kafka` 镜像以非 root 用户运行，**不要给其日志目录挂载数据卷**——命名卷初始化为 root 属主（755），Kafka 格式化元数据会抛 `AccessDeniedException` 直接退出；让 Kafka 用容器内默认目录即可（见第一节 compose）。

## 八、涉及文件清单

- 新增：根目录 `docker-compose.yml`
- 修改：`pom.xml`、`src/main/resources/application.yml`、`src/main/java/com/chaoxing/template/user/service/impl/UserServiceImpl.java`、`src/test/java/com/chaoxing/template/user/service/impl/UserServiceImplTest.java`、`README.md`
- 新增（`src/main/java/com/chaoxing/template/kafka`）：`config/KafkaConfig.java`、`dto/DemoMessage.java`、`dto/UserCreatedEvent.java`、`dto/KafkaMessageRequest.java`、`producer/KafkaDemoProducer.java`、`producer/KafkaEventPublisher.java`、`consumer/PlainMessageConsumer.java`、`consumer/KeyedMessageConsumer.java`、`consumer/ManualAckConsumer.java`、`consumer/RetryableMessageConsumer.java`、`consumer/UserCreatedEventConsumer.java`、`controller/KafkaDemoController.java`
- 新增测试：`kafka/producer/KafkaEventPublisherTest.java`、`kafka/producer/KafkaDemoProducerTest.java`、`kafka/controller/KafkaDemoControllerTest.java`、`kafka/KafkaRoundTripTest.java`
