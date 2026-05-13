# ObservAI

ObservAI 是根据 `AIOps_PRD.md` 搭建的 AI 运维助手演示框架。当前版本优先保证架构清晰、模块完整、职责边界正确，核心业务数据已接入 MySQL 持久化，复杂外部能力仍使用可替换的 Mock 实现托底，后续可以继续接入真实阿里云 AI API 和真实 SMTP 发送。

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

## Docker Compose 运行

推荐用 Docker Compose 启动整套系统：

```bash
docker compose up --build
```

首次构建会下载 Maven、Node 和镜像依赖，耗时较长。之后如果没有改 Dockerfile 或依赖，普通启动即可：

```bash
docker compose up
```

后台启动：

```bash
docker compose up -d
```

停止服务但保留数据库数据：

```bash
docker compose down
```

清空数据库数据并重新初始化：

```bash
docker compose down -v
docker compose up --build
```

启动后访问：

- 前端：http://localhost:3000
- Gateway：http://localhost:8080
- Nacos：http://localhost:8848/nacos
- MySQL：`localhost:3307`
- 数据库名：`observai`
- 数据库用户：`observai`
- 数据库密码：`observai`
- Root 密码：`root`

## 数据库初始化与持久化

数据库使用 Docker 启动，配置在 `docker-compose.yml` 的 `mysql` 服务中。

初始化 SQL：

- `docker/mysql/01-schema.sql`：建表语句
- `docker/mysql/02-data.sql`：初始化数据

MySQL 数据已通过 Docker volume 持久化：

```yaml
volumes:
  - observai-mysql-data:/var/lib/mysql
```

这意味着：

- 首次创建数据库时会自动执行 `docker/mysql` 下的 SQL 文件。
- 后续 `docker compose up` 不会重复初始化数据库。
- `docker compose down` 不会删除数据库数据。
- 只有执行 `docker compose down -v` 才会删除数据库 volume 并重新初始化。

## Maven 构建说明

后端 Docker 构建使用 Maven 镜像：

```text
maven:3.9.9-eclipse-temurin-17
```

为了减少依赖下载失败和重复下载，已配置：

- Maven 镜像源：`docker/maven/settings.xml`
- Docker BuildKit Maven 本地仓库缓存：`/root/.m2/repository`

如果遇到 Maven 依赖下载失败，例如：

```text
Remote host terminated the handshake
```

可以清理 Docker 构建缓存后重试：

```bash
docker builder prune
docker compose up --build
```

如果本机安装了 Maven，也可以直接执行：

```bash
mvn -DskipTests package
```

## 本地前端开发

只开发前端时：

```bash
cd frontend
npm install
npm run dev
```

访问：

```text
http://localhost:5173/login
```

前端开发服务默认将 `/api` 请求代理到：

```text
http://localhost:8080
```

所以只跑前端时，Gateway 也需要在 `localhost:8080` 上运行。

## 环境变量

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
- `user-service` 已从 MySQL 读取账号，并使用 BCrypt 校验密码。
- `monitor-service` 已将告警规则和服务指标写入 MySQL。
- `alert-service` 已将告警记录和状态历史写入 MySQL。
- `notification-service` 已将通知配置和通知记录写入 MySQL。
- 告警服务已支持指纹去重、触发次数更新、异步 Mock 诊断、状态历史记录。
- 通知服务已支持配置管理、级别过滤、发送结果记录；真实 SMTP 发送位置已预留。
