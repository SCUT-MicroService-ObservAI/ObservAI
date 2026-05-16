# ObservAI 开发任务清单

目标：完成《AI 运维助手》最终版本开发，使系统具备完整的微服务监控、告警生成、告警去重与抑制、异步 AI 诊断、邮件通知、前端可视化和 Docker Compose 一键部署能力。

## 1. 工程与启动

- 修复并确保后端 Maven 全量编译通过。
- 确保所有模块可以通过 Docker Compose 一键构建和启动。
- 保持当前微服务拆分结构：
  - `gateway-service`
  - `user-service`
  - `monitor-service`
  - `alert-service`
  - `notification-service`
  - `monitored-services/demo-user-service`
  - `monitored-services/demo-order-service`
  - `monitored-services/demo-payment-service`
  - `frontend`
- 补齐 `.env.example`，列出所有必要环境变量。
- 补齐启动、停止、重建、排障相关脚本或文档。

## 2. 数据库持久化

- 将当前内存仓储替换为 MySQL 持久化。
- 完成并校验以下表的实体、Mapper/Repository、服务层逻辑：
  - `users`
  - `service_metrics`
  - `alert_rule`
  - `alerts`
  - `alert_status_history`
  - `notification_config`
  - `notification_record`
- 完善数据库初始化脚本，包含必要表结构、索引和演示数据。
- 保证服务重启后业务数据不丢失。

## 3. 用户登录与网关鉴权

- 完成用户登录功能，从数据库读取用户信息。
- 密码使用 BCrypt 加密存储和校验。
- 登录成功后返回 JWT 和过期时间。
- Gateway 统一校验 JWT。
- `/login` 放行，其余业务接口必须鉴权。
- 系统不引入 admin/operator 角色区分。
- 统一处理 JWT 缺失、无效、过期等异常。

## 4. 被监控服务

- 三个 demo 服务提供稳定接口：
  - `GET /health`
  - `GET /metrics`
  - `GET /test/error`
  - `GET /test/slow`
- 支持模拟错误率升高、响应时间升高、服务异常等演示场景。
- 保持服务注册名不变：
  - `demo-user-service`
  - `demo-order-service`
  - `demo-payment-service`

## 5. 监控服务

- 定时轮询被监控服务的 `/health` 和 `/metrics`。
- 保存服务最新状态和指标历史。
- 从数据库读取启用状态的告警规则。
- 支持 CPU、内存、错误率、响应时间、服务在线状态等指标判断。
- 支持比较符：
  - `>`
  - `>=`
  - `<`
  - `<=`
  - `==`
- 实现 `durationSeconds` 持续时间判断。
- 指标异常时获取异常日志片段。
- 调用 `alert-service` 上报告警。
- 被监控服务调用失败时，监控任务不能整体阻塞。
- 接入 Sentinel 限流和降级保护。

## 6. 告警规则配置

- 完成告警规则 CRUD。
- 支持启用、停用规则。
- 修改规则后无需重启服务即可生效。
- 后端校验规则字段合法性。
- 前端提供规则列表、创建、编辑、删除、启停操作。

## 7. 告警生成、去重与抑制

- `alert-service` 接收 `monitor-service` 上报的异常数据。
- 根据 `serviceName + alertType + metricName` 生成告警指纹。
- 相同指纹且未结束的告警不重复创建。
- 重复触发时更新：
  - `trigger_count`
  - `last_triggered_at`
  - `metrics_snapshot`
  - `log_snippet`
- 实现告警抑制窗口，窗口内不重复发送通知。
- 终态告警不参与未结束告警去重。
- 新告警默认状态为 `UNHANDLED`。

## 8. 告警生命周期

- 支持告警状态：
  - `UNHANDLED`
  - `PROCESSING`
  - `RESOLVED`
  - `IGNORED`
  - `FALSE_ALARM`
  - `RECOVERED`
- 完成告警状态修改接口。
- 校验非法状态值。
- 每次状态变化写入 `alert_status_history`。
- 支持系统自动标记恢复状态。
- 前端告警详情页展示状态历史。

## 9. AI 异步诊断

- 告警创建接口不得等待 AI 诊断完成。
- 使用异步线程池执行诊断任务。
- 诊断状态完整流转：
  - `PENDING`
  - `RUNNING`
  - `SUCCESS`
  - `FAILED`
  - `MOCKED`
- 接入阿里云 AI API。
- AI Key 从环境变量读取，不能写死在代码中。
- 封装 Prompt 生成逻辑。
- 要求 AI 返回结构化 JSON。
- 解析并保存 AI 诊断结果。
- AI 调用失败、超时、格式异常时生成 Mock 诊断结果。
- 前端展示诊断状态和诊断报告。

## 10. 邮件通知

- 完成邮件通知配置 CRUD。
- 支持启用、停用邮箱配置。
- 根据告警严重等级和最低通知等级判断是否发送。
- 接入 `JavaMailSender` 发送真实邮件。
- SMTP 配置从环境变量读取。
- 每次发送或跳过都保存通知记录。
- 发送状态包括：
  - `PENDING`
  - `SUCCESS`
  - `FAILED`
  - `SKIPPED`
- 邮件发送失败不能影响告警主流程。
- 前端展示通知配置和通知记录。

## 11. 前端页面

- 完成登录页。
- 完成服务状态首页：
  - 总服务数
  - 异常服务数
  - 未处理告警数
  - 服务状态卡片
  - 自动刷新
- 完成告警列表页：
  - 状态筛选
  - 服务名筛选
  - 严重等级筛选
  - 时间范围筛选
  - 告警详情跳转
- 完成告警详情页：
  - 基础信息
  - 指标快照
  - 异常日志
  - AI 诊断状态
  - AI 诊断结果
  - 当前处理状态
  - 状态修改
  - 状态历史
  - 通知记录
- 完成告警配置页：
  - 告警规则配置 Tab
  - 邮件通知配置 Tab
- 完成通知记录页。
- 前端不展示角色差异。

## 12. 接口与异常规范

- 所有后端接口统一返回：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

- 统一处理参数错误、鉴权失败、资源不存在、系统异常。
- 保持错误码与 PRD 一致。
- Feign 调用失败要有降级或明确异常处理。
- 关键业务流程必须记录日志。

## 13. Docker 与部署

- Docker Compose 启动以下服务：
  - Nacos
  - MySQL
  - Gateway
  - 所有后端微服务
  - 三个被监控服务
  - Frontend
- 确保服务之间通过 Nacos 注册发现。
- 前端容器通过 Gateway 访问后端。
- 环境变量覆盖敏感配置：
  - `JWT_SECRET`
  - `ALIYUN_AI_API_KEY`
  - `MAIL_HOST`
  - `MAIL_PORT`
  - `MAIL_USERNAME`
  - `MAIL_PASSWORD`
- 为关键容器补充健康检查。

## 14. 测试与验收

- 补充核心单元测试：
  - 规则判断
  - 告警去重
  - 告警抑制
  - 状态流转
  - 严重等级比较
  - AI Mock 兜底
- 补充接口集成测试：
  - 登录
  - 查询服务状态
  - 创建告警
  - 查询告警列表
  - 查询告警详情
  - 修改告警状态
  - 管理告警规则
  - 管理通知配置
  - 查询通知记录
- 完成端到端演示场景：
  - 错误率过高
  - 服务下线
  - 响应时间过高
  - 重复告警抑制
- 最终验收标准：`docker compose up --build` 后，用户可以登录系统，查看服务状态，触发告警，查看 AI 诊断结果，处理告警状态，配置规则和邮件通知，并查看通知记录。

