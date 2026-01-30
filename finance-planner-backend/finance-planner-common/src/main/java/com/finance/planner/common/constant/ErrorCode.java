package com.finance.planner.common.constant;

import lombok.Getter;

@Getter
public enum ErrorCode {

    // Success
    SUCCESS(200, "success"),

    // Client Errors (4xx)
    BAD_REQUEST(400, "Bad request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Resource not found"),
    METHOD_NOT_ALLOWED(405, "Method not allowed"),
    VALIDATION_ERROR(422, "Validation error"),

    // Authentication Errors (1xxx)
    USER_NOT_FOUND(1001, "User not found"),
    INVALID_CREDENTIALS(1002, "Invalid username or password"),
    USERNAME_EXISTS(1003, "Username already exists"),
    EMAIL_EXISTS(1004, "Email already exists"),
    TOKEN_EXPIRED(1005, "Token has expired"),
    TOKEN_INVALID(1006, "Invalid token"),
    USER_DISABLED(1007, "User account is disabled"),

    // Transaction Errors (2xxx)
    TRANSACTION_NOT_FOUND(2001, "Transaction not found"),
    INVALID_AMOUNT(2002, "Invalid transaction amount"),
    INVALID_TRANSACTION_TYPE(2003, "Invalid transaction type"),
    INVALID_DATE(2004, "Invalid transaction date"),

    // Category Errors (3xxx)
    CATEGORY_NOT_FOUND(3001, "Category not found"),
    CATEGORY_IN_USE(3002, "Category is in use and cannot be deleted"),
    SYSTEM_CATEGORY_PROTECTED(3003, "System category cannot be modified"),

    // AI Errors (4xxx)
    AI_RATE_LIMIT_EXCEEDED(4001, "今日对话次数已用完"),
    AI_SERVICE_UNAVAILABLE(4002, "AI 服务暂时不可用"),
    AI_SESSION_NOT_FOUND(4003, "对话会话不存在"),
    AI_MESSAGE_TOO_LONG(4004, "消息超出最大长度"),
    AI_PROVIDER_NOT_FOUND(4005, "AI 模型提供商不存在"),

    // Server Errors (5xxx)
    INTERNAL_ERROR(500, "Internal server error"),
    SERVICE_UNAVAILABLE(503, "Service temporarily unavailable");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
