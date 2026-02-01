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

    // Goal Errors (5xxx - business)
    GOAL_NOT_FOUND(5001, "理财目标不存在"),
    GOAL_ALREADY_COMPLETED(5002, "该目标已完成"),
    GOAL_DEADLINE_PAST(5003, "目标截止日期已过"),
    GOAL_INVALID_AMOUNT(5004, "目标金额无效"),

    // OCR Errors (6xxx)
    OCR_RECOGNITION_FAILED(6001, "OCR识别失败"),
    OCR_EMPTY_RESULT(6002, "未识别到有效信息"),
    OCR_RECORD_NOT_FOUND(6003, "OCR记录不存在"),
    OCR_ALREADY_CONFIRMED(6004, "该记录已确认"),
    OCR_ALREADY_REJECTED(6005, "该记录已拒绝"),
    OCR_FILE_TOO_LARGE(6006, "文件大小超出限制"),

    // Career Errors (7xxx)
    CAREER_PLAN_NOT_FOUND(7001, "副业计划不存在"),
    CAREER_PROFILE_INCOMPLETE(7002, "请先完善个人资料"),
    CAREER_PROFILE_NOT_FOUND(7003, "个人资料不存在"),
    CAREER_RECOMMENDATION_FAILED(7004, "AI推荐生成失败"),

    // Budget Errors (8xxx)
    BUDGET_NOT_FOUND(8001, "预算记录不存在"),
    BUDGET_DUPLICATE(8002, "该分类本月预算已存在"),
    BUDGET_INVALID_MONTH(8003, "无效的月份格式"),
    BUDGET_NO_PREVIOUS(8004, "上月没有预算记录可复制"),

    // Investment Errors (9xxx)
    RISK_ASSESSMENT_NOT_FOUND(9001, "风险评估不存在"),
    RISK_QUIZ_INVALID(9002, "风险评估问卷答案无效"),
    INVESTMENT_RECOMMENDATION_FAILED(9003, "投资建议生成失败"),
    INVESTMENT_NO_ASSESSMENT(9004, "请先完成风险评估"),

    // Server Errors
    INTERNAL_ERROR(500, "Internal server error"),
    SERVICE_UNAVAILABLE(503, "Service temporarily unavailable");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
