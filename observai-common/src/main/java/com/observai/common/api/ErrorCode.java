package com.observai.common.api;

public enum ErrorCode {
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或 Token 无效"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "系统异常"),
    INVALID_ALERT_STATUS(1001, "告警状态非法"),
    ALERT_RULE_NOT_FOUND(1002, "告警规则不存在"),
    AI_DIAGNOSIS_MOCKED(2001, "AI 诊断失败，已使用 Mock"),
    MAIL_SEND_FAILED(3001, "邮件发送失败");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }
}

