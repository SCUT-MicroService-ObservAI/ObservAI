CREATE DATABASE IF NOT EXISTS observai
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE observai;

-- 保证 init 脚本里的中文 COMMENT / 字符串按 UTF-8 写入，避免首次导入出现乱码
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
  user_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  username VARCHAR(100) NOT NULL UNIQUE COMMENT '用户名',
  password VARCHAR(255) NOT NULL COMMENT 'BCrypt 加密后的密码',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS service_metrics (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  service_name VARCHAR(100) NOT NULL COMMENT '服务名称',
  cpu FLOAT NOT NULL DEFAULT 0 COMMENT 'CPU 使用率',
  memory FLOAT NOT NULL DEFAULT 0 COMMENT '内存使用率',
  request_count INT NOT NULL DEFAULT 0 COMMENT '请求数',
  error_rate FLOAT NOT NULL DEFAULT 0 COMMENT '错误率',
  response_time FLOAT NOT NULL DEFAULT 0 COMMENT '响应时间，单位毫秒',
  status VARCHAR(30) NOT NULL COMMENT 'UP / DOWN / ABNORMAL',
  timestamp DATETIME NOT NULL COMMENT '采集时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_service_metrics_name_time (service_name, timestamp),
  INDEX idx_service_metrics_status_time (status, timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务指标历史表';

CREATE TABLE IF NOT EXISTS alert_rule (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  service_name VARCHAR(100) NOT NULL COMMENT '服务名称',
  metric_name VARCHAR(50) NOT NULL COMMENT '指标名称',
  operator VARCHAR(10) NOT NULL COMMENT '比较符，例如 >、>=、<、<=、==',
  threshold DOUBLE NOT NULL COMMENT '阈值',
  duration_seconds INT NOT NULL DEFAULT 0 COMMENT '持续时间，单位秒',
  severity VARCHAR(30) NOT NULL COMMENT 'LOW / MEDIUM / HIGH / CRITICAL',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_alert_rule_enabled_service (enabled, service_name),
  UNIQUE KEY uk_alert_rule_service_metric_operator (service_name, metric_name, operator)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警规则表';

CREATE TABLE IF NOT EXISTS alerts (
  alert_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '告警 ID',
  service_name VARCHAR(100) NOT NULL COMMENT '服务名称',
  alert_type VARCHAR(100) NOT NULL COMMENT '告警类型',
  metric_name VARCHAR(50) NOT NULL COMMENT '指标名称',
  severity VARCHAR(30) NOT NULL COMMENT '严重等级',
  status VARCHAR(30) NOT NULL COMMENT '告警状态',
  fingerprint VARCHAR(255) NOT NULL COMMENT '告警指纹，用于去重',
  trigger_count INT NOT NULL DEFAULT 1 COMMENT '触发次数',
  metrics_snapshot JSON COMMENT '指标快照',
  log_snippet TEXT COMMENT '异常日志片段',
  diagnosis_status VARCHAR(30) NOT NULL COMMENT '诊断状态',
  diagnosis_result JSON COMMENT 'AI 或 Mock 诊断结果',
  first_triggered_at DATETIME NOT NULL COMMENT '首次触发时间',
  last_triggered_at DATETIME NOT NULL COMMENT '最近触发时间',
  recovered_at DATETIME NULL COMMENT '恢复时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_alerts_fingerprint_status (fingerprint, status),
  INDEX idx_alerts_service_time (service_name, created_at),
  INDEX idx_alerts_status_severity (status, severity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警记录表';

CREATE TABLE IF NOT EXISTS alert_status_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  alert_id BIGINT NOT NULL COMMENT '告警 ID',
  from_status VARCHAR(30) NULL COMMENT '原状态',
  to_status VARCHAR(30) NOT NULL COMMENT '新状态',
  operator VARCHAR(100) NOT NULL DEFAULT 'system' COMMENT '操作人',
  remark VARCHAR(500) NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_alert_status_history_alert_id (alert_id),
  CONSTRAINT fk_alert_status_history_alert
    FOREIGN KEY (alert_id) REFERENCES alerts(alert_id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警状态历史表';

CREATE TABLE IF NOT EXISTS notification_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  email VARCHAR(255) NOT NULL COMMENT '接收邮箱',
  min_severity VARCHAR(30) NOT NULL COMMENT '最低通知级别',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_notification_config_email (email),
  INDEX idx_notification_config_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮件通知配置表';

CREATE TABLE IF NOT EXISTS notification_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  alert_id BIGINT NULL COMMENT '告警 ID',
  email VARCHAR(255) NULL COMMENT '接收邮箱',
  title VARCHAR(255) NOT NULL COMMENT '邮件标题',
  content TEXT COMMENT '邮件内容',
  status VARCHAR(30) NOT NULL COMMENT 'PENDING / SUCCESS / FAILED / SKIPPED',
  error_message TEXT COMMENT '失败原因或跳过原因',
  sent_at DATETIME NULL COMMENT '发送时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_notification_record_alert_id (alert_id),
  INDEX idx_notification_record_status_time (status, created_at),
  CONSTRAINT fk_notification_record_alert
    FOREIGN KEY (alert_id) REFERENCES alerts(alert_id)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮件通知记录表';

