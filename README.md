# ObservAI

ObservAI 是根据 `AIOps_PRD.md` 搭建的 AI 运维助手演示框架。当前版本优先保证架构清晰、模块完整、职责边界正确，复杂业务细节使用可替换的内存仓储与 Mock 实现托底，后续可以逐步接入 MySQL、真实阿里云 AI API 和真实 SMTP 发送。

## 模块划分

| 模块 | 职责 |
|---|---|
| `observai-common` | 统一响应、错误码、公共 DTO、领域枚举、JWT 工具 |
| `gateway-service` | 统一入口、JWT 鉴权、路由转发、Nacos 服务发现 |
| `user-service` | 登录、BCrypt 密码校验、JWT 签发 |
| `monitor-service` | 定时采集 demo 服务状态、规则判断、异常上报、规则配置 API |
| `alert-service` | 告警生成、指纹去重、状态生命周期、异步诊断、触发通知 |
| `notification-service` | 邮件通知配置、发送判定、通知记录 |
| `monitored-services/demo-*-service` | 模拟业务微服务，提供 `/health`、`/metrics`、`/test/error`、`/test/slow` |
| `frontend` | Vue 3 + Element Plus 运维工作台 |

## 默认账号

```text
账号：ops
密码：123456
```

## 本地开发

后端需要 JDK 17+ 和 Maven 3.9+：

```bash
mvn -DskipTests package
```

前端：

```bash
cd frontend
npm install
npm run dev
```

开发环境前端默认代理到 `http://localhost:8080`。

## Docker Compose

```bash
docker compose up --build
```

启动后访问：

- 前端：http://localhost:3000
- Gateway：http://localhost:8080
- Nacos：http://localhost:8848/nacos
- MySQL：localhost:3306，数据库 `observai`

可通过环境变量注入敏感配置：

```bash
JWT_SECRET=your-jwt-secret
ALIYUN_AI_API_KEY=your-api-key
MAIL_HOST=smtp.example.com
MAIL_PORT=465
MAIL_USERNAME=ops@example.com
MAIL_PASSWORD=your-mail-auth-code
```

## 当前实现说明

- 已按 PRD 拆分微服务和前端页面，并保留清晰的 Service / Repository / Client 边界。
- 后端接口统一返回 `code`、`message`、`data`。
- 告警服务已支持指纹去重、触发次数更新、异步 Mock 诊断、状态历史记录。
- 通知服务已支持配置管理、级别过滤、发送结果记录；真实 SMTP 发送位置已预留。
- `docker/mysql/01-schema.sql` 和 `docker/mysql/02-data.sql` 已提供 PRD 对应表结构与初始化数据，当前 Java 代码为了便于框架演示先使用内存仓储。
