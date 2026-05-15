USE observai;

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

INSERT INTO users (user_id, username, password)
VALUES
  (1, 'ops', '$2a$10$0xKQJlgeZqGaNk2iAXFJOOQCpvRnshv.1KOxNT8Xcu8gkAdUMalza')
ON DUPLICATE KEY UPDATE
  username = VALUES(username),
  password = VALUES(password);

INSERT INTO alert_rule (id, service_name, metric_name, operator, threshold, duration_seconds, severity, enabled)
VALUES
  (1, 'demo-order-service', 'errorRate', '>', 10, 60, 'HIGH', 1),
  (2, 'demo-order-service', 'cpu', '>', 80, 60, 'MEDIUM', 1),
  (3, 'demo-payment-service', 'responseTime', '>', 1000, 60, 'HIGH', 1),
  (4, 'demo-payment-service', 'errorRate', '>', 8, 60, 'HIGH', 1),
  (5, 'demo-user-service', 'memory', '>', 85, 120, 'MEDIUM', 1)
ON DUPLICATE KEY UPDATE
  threshold = VALUES(threshold),
  duration_seconds = VALUES(duration_seconds),
  severity = VALUES(severity),
  enabled = VALUES(enabled);

INSERT INTO service_metrics
  (service_name, cpu, memory, request_count, error_rate, response_time, status, timestamp)
VALUES
  ('demo-user-service', 31.5, 48.2, 1250, 0.8, 135, 'UP', NOW() - INTERVAL 5 MINUTE),
  ('demo-order-service', 86.4, 72.1, 3420, 12.6, 580, 'ABNORMAL', NOW() - INTERVAL 4 MINUTE),
  ('demo-payment-service', 58.7, 66.5, 2180, 2.1, 1180, 'ABNORMAL', NOW() - INTERVAL 3 MINUTE);

INSERT INTO alerts
  (alert_id, service_name, alert_type, metric_name, severity, status, fingerprint, trigger_count,
   metrics_snapshot, log_snippet, diagnosis_status, diagnosis_result,
   first_triggered_at, last_triggered_at, last_notified_at, created_at, updated_at)
VALUES
  (
    1001,
    'demo-order-service',
    'ERROR_RATE_HIGH',
    'errorRate',
    'HIGH',
    'UNHANDLED',
    'demo-order-service:ERROR_RATE_HIGH:errorRate',
    3,
    JSON_OBJECT('cpu', 86.4, 'memory', 72.1, 'requestCount', 3420, 'errorRate', 12.6, 'responseTime', 580),
    'java.lang.RuntimeException: order create failed, database connection timeout',
    'MOCKED',
    JSON_OBJECT(
      'faultType', '接口错误率过高',
      'rootCause', '订单服务运行时异常导致错误率升高',
      'impactScope', JSON_ARRAY('demo-order-service'),
      'suggestionSteps', JSON_ARRAY('检查订单创建接口异常日志', '检查数据库连接是否正常', '确认最近是否有发布变更'),
      'rollbackSuggestion', '如果异常由最近发布引起，建议回滚到上一稳定版本',
      'severity', 'HIGH',
      'confidence', 0.82,
      'needManualHandle', true,
      'diagnosedAt', DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'),
      'source', 'MOCK'
    ),
    NOW() - INTERVAL 4 MINUTE,
    NOW() - INTERVAL 1 MINUTE,
    NULL,
    NOW() - INTERVAL 4 MINUTE,
    NOW() - INTERVAL 1 MINUTE
  )
ON DUPLICATE KEY UPDATE
  trigger_count = VALUES(trigger_count),
  metrics_snapshot = VALUES(metrics_snapshot),
  log_snippet = VALUES(log_snippet),
  diagnosis_status = VALUES(diagnosis_status),
  diagnosis_result = VALUES(diagnosis_result),
  last_triggered_at = VALUES(last_triggered_at),
  updated_at = VALUES(updated_at);

INSERT INTO alert_status_history
  (id, alert_id, from_status, to_status, operator, remark, created_at)
VALUES
  (1, 1001, NULL, 'UNHANDLED', 'system', '告警首次创建', NOW() - INTERVAL 4 MINUTE)
ON DUPLICATE KEY UPDATE
  to_status = VALUES(to_status),
  remark = VALUES(remark);

INSERT INTO notification_config (id, email, min_severity, enabled)
VALUES
  (1, 'ops@example.com', 'HIGH', 1),
  (2, 'sre@example.com', 'CRITICAL', 1)
ON DUPLICATE KEY UPDATE
  min_severity = VALUES(min_severity),
  enabled = VALUES(enabled);

INSERT INTO notification_record
  (id, alert_id, email, title, content, status, error_message, sent_at, created_at)
VALUES
  (
    1,
    1001,
    'ops@example.com',
    '【HIGH 告警】demo-order-service ERROR_RATE_HIGH',
    '服务名称：demo-order-service\n告警类型：ERROR_RATE_HIGH\n严重等级：HIGH\nAI 诊断：订单服务运行时异常导致错误率升高\n处理建议：检查订单创建接口异常日志、数据库连接和最近发布记录',
    'SKIPPED',
    '演示数据：未配置真实 SMTP，未实际发送',
    NULL,
    NOW() - INTERVAL 1 MINUTE
  )
ON DUPLICATE KEY UPDATE
  status = VALUES(status),
  error_message = VALUES(error_message),
  created_at = VALUES(created_at);

