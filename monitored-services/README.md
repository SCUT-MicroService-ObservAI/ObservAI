# Demo Services - 测试接口文档

本文档描述三个被监控 demo 服务的测试接口实现。

## 服务列表

| 服务名称 | 端口 | 包路径 |
|---------|------|--------|
| demo-user-service | 8091 | com.observai.demo.user |
| demo-order-service | 8092 | com.observai.demo.order |
| demo-payment-service | 8093 | com.observai.demo.payment |

## 接口说明

### 1. 健康检查接口

**端点:** `GET /health`

**描述:** 返回服务在线状态

**响应示例:**
```json
{
  "status": "UP"
}
```

**响应时间:** < 100ms

---

### 2. 运行指标接口

**端点:** `GET /metrics`

**描述:** 返回服务运行指标（动态生成）

**响应示例:**
```json
{
  "cpu": 45.3,
  "memory": 62.7,
  "requestCount": 1234,
  "errorRate": 5.8,
  "responseTime": 245.6
}
```

**字段说明:**
- `cpu`: CPU 使用率百分比 (15% - 85%)
- `memory`: 内存使用率百分比 (20% - 80%)
- `requestCount`: 请求总数 (500 - 2000)
- `errorRate`: 错误率百分比 (0% - 15%)
- `responseTime`: 平均响应时间，单位毫秒 (80ms - 600ms)

**特性:**
- 每次请求返回不同的随机值
- 值在合理范围内波动，模拟真实场景

---

### 3. 异常日志模拟接口

**端点:** `GET /test/error`

**描述:** 返回模拟的异常日志片段

**Content-Type:** `text/plain`

**响应示例 (demo-user-service):**
```
2024-01-15 10:23:45.123 ERROR [demo-user-service] [http-nio-8091-exec-1] c.o.d.u.service.UserService : 用户服务异常
java.lang.RuntimeException: Failed to query user information from database
	at com.observai.demo.user.service.UserService.getUserById(UserService.java:45)
	at com.observai.demo.user.controller.UserController.getUser(UserController.java:28)
	...
Caused by: java.sql.SQLException: Connection timeout
	at com.mysql.cj.jdbc.ConnectionImpl.connectWithRetries(ConnectionImpl.java:876)
	...
```

**响应示例 (demo-order-service):**
```
2024-01-15 10:23:45.123 ERROR [demo-order-service] [http-nio-8092-exec-1] c.o.d.o.service.OrderService : 订单服务异常
java.lang.RuntimeException: Failed to create order - order create failed
	at com.observai.demo.order.service.OrderService.createOrder(OrderService.java:52)
	...
Caused by: com.observai.common.exception.BusinessException: Inventory check failed
	...
```

**响应示例 (demo-payment-service):**
```
2024-01-15 10:23:45.123 ERROR [demo-payment-service] [http-nio-8093-exec-1] c.o.d.p.service.PaymentService : 支付服务异常
java.lang.RuntimeException: Payment processing failed - third party payment gateway timeout
	at com.observai.demo.payment.service.PaymentService.processPayment(PaymentService.java:67)
	...
Caused by: java.net.SocketTimeoutException: Read timed out
	...
```

**特性:**
- 每个服务返回不同的业务异常日志
- 包含完整的 Java 异常堆栈信息
- 包含实时时间戳

---

### 4. 慢接口模拟

**端点:** `GET /test/slow`

**描述:** 模拟响应时间过长的场景

**响应示例:**
```json
{
  "result": "completed",
  "delayMs": 950
}
```

**特性:**
- 延迟时间: 800ms - 1200ms（随机）
- 用于触发响应时间过高告警

---

## 本地测试

### 启动服务

```bash
# 启动 demo-user-service
cd monitored-services/demo-user-service
mvn spring-boot:run

# 启动 demo-order-service
cd monitored-services/demo-order-service
mvn spring-boot:run

# 启动 demo-payment-service
cd monitored-services/demo-payment-service
mvn spring-boot:run
```

### 测试接口

```bash
# 测试健康检查
curl http://localhost:8091/health
curl http://localhost:8092/health
curl http://localhost:8093/health

# 测试运行指标
curl http://localhost:8091/metrics
curl http://localhost:8092/metrics
curl http://localhost:8093/metrics

# 测试异常日志
curl http://localhost:8091/test/error
curl http://localhost:8092/test/error
curl http://localhost:8093/test/error

# 测试慢接口
curl http://localhost:8091/test/slow
curl http://localhost:8092/test/slow
curl http://localhost:8093/test/slow
```

## Docker Compose 部署

服务已配置在 `docker-compose.yml` 中，通过以下命令启动：

```bash
docker compose up --build
```

服务会自动注册到 Nacos，monitor-service 可以通过服务发现调用这些接口。

## 演示场景支持

### 1. 错误率升高场景
- `/metrics` 接口的 `errorRate` 字段可能返回 > 10% 的值
- 配合 `/test/error` 接口获取异常日志

### 2. 响应时间升高场景
- `/metrics` 接口的 `responseTime` 字段可能返回 > 500ms 的值
- `/test/slow` 接口实际延迟 800-1200ms

### 3. 服务下线场景
- 停止服务容器：`docker compose stop demo-order-service`
- `/health` 接口不可访问
- Nacos 标记服务为下线状态

## 技术实现

### 架构
- **Controller 层**: 处理 HTTP 请求，定义接口路由
- **Service 层**: 业务逻辑，动态指标生成
- **Model 层**: 数据模型，响应对象

### 关键特性
1. **动态指标生成**: 使用 `Random` 在合理范围内生成随机值
2. **服务差异化**: 每个服务返回不同的业务异常日志
3. **日志记录**: 关键操作记录到日志文件
4. **Nacos 集成**: 自动服务注册与发现

### 依赖
- Spring Boot 3.x
- Spring Cloud Alibaba Nacos Discovery
- SLF4J 日志框架

## 注意事项

1. **端口冲突**: 确保 8091、8092、8093 端口未被占用
2. **Nacos 连接**: 确保 Nacos 服务器可访问（默认 localhost:8848）
3. **日志级别**: 默认 DEBUG 级别，生产环境建议调整为 INFO
4. **线程阻塞**: `/test/slow` 接口会阻塞线程，不要在生产环境使用

## 后续扩展

可选的增强功能：
- [ ] 添加 Spring Boot Actuator 端点
- [ ] 支持配置化的指标范围
- [ ] 添加更多异常类型模拟
- [ ] 支持手动触发高错误率模式
- [ ] 添加接口调用统计

---

**创建时间:** 2024
**维护者:** ObservAI Team
