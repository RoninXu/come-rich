package com.finance.planner.common.constant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ErrorCode Enum Tests")
class ErrorCodeTest {

    @Test
    @DisplayName("SUCCESS code should be 200 with correct message")
    void testSuccessCode() {
        assertEquals(200, ErrorCode.SUCCESS.getCode());
        assertEquals("success", ErrorCode.SUCCESS.getMessage());
    }

    @Test
    @DisplayName("Client error codes should have correct codes and messages")
    void testClientErrorCodes() {
        assertEquals(400, ErrorCode.BAD_REQUEST.getCode());
        assertEquals("Bad request", ErrorCode.BAD_REQUEST.getMessage());

        assertEquals(401, ErrorCode.UNAUTHORIZED.getCode());
        assertEquals("Unauthorized", ErrorCode.UNAUTHORIZED.getMessage());

        assertEquals(403, ErrorCode.FORBIDDEN.getCode());
        assertEquals("Forbidden", ErrorCode.FORBIDDEN.getMessage());

        assertEquals(404, ErrorCode.NOT_FOUND.getCode());
        assertEquals("Resource not found", ErrorCode.NOT_FOUND.getMessage());

        assertEquals(405, ErrorCode.METHOD_NOT_ALLOWED.getCode());
        assertEquals("Method not allowed", ErrorCode.METHOD_NOT_ALLOWED.getMessage());

        assertEquals(422, ErrorCode.VALIDATION_ERROR.getCode());
        assertEquals("Validation error", ErrorCode.VALIDATION_ERROR.getMessage());
    }

    @Test
    @DisplayName("Authentication error codes should be in the 1xxx range with correct messages")
    void testAuthErrorCodes() {
        assertEquals(1001, ErrorCode.USER_NOT_FOUND.getCode());
        assertEquals("User not found", ErrorCode.USER_NOT_FOUND.getMessage());

        assertEquals(1002, ErrorCode.INVALID_CREDENTIALS.getCode());
        assertEquals("Invalid username or password", ErrorCode.INVALID_CREDENTIALS.getMessage());

        assertEquals(1003, ErrorCode.USERNAME_EXISTS.getCode());
        assertEquals("Username already exists", ErrorCode.USERNAME_EXISTS.getMessage());

        assertEquals(1004, ErrorCode.EMAIL_EXISTS.getCode());
        assertEquals("Email already exists", ErrorCode.EMAIL_EXISTS.getMessage());

        assertEquals(1005, ErrorCode.TOKEN_EXPIRED.getCode());
        assertEquals("Token has expired", ErrorCode.TOKEN_EXPIRED.getMessage());

        assertEquals(1006, ErrorCode.TOKEN_INVALID.getCode());
        assertEquals("Invalid token", ErrorCode.TOKEN_INVALID.getMessage());

        assertEquals(1007, ErrorCode.USER_DISABLED.getCode());
        assertEquals("User account is disabled", ErrorCode.USER_DISABLED.getMessage());
    }

    @Test
    @DisplayName("Transaction and category error codes should be in the 2xxx/3xxx range")
    void testTransactionErrorCodes() {
        // Transaction errors (2xxx)
        assertEquals(2001, ErrorCode.TRANSACTION_NOT_FOUND.getCode());
        assertEquals("Transaction not found", ErrorCode.TRANSACTION_NOT_FOUND.getMessage());

        assertEquals(2002, ErrorCode.INVALID_AMOUNT.getCode());
        assertEquals("Invalid transaction amount", ErrorCode.INVALID_AMOUNT.getMessage());

        assertEquals(2003, ErrorCode.INVALID_TRANSACTION_TYPE.getCode());
        assertEquals("Invalid transaction type", ErrorCode.INVALID_TRANSACTION_TYPE.getMessage());

        assertEquals(2004, ErrorCode.INVALID_DATE.getCode());
        assertEquals("Invalid transaction date", ErrorCode.INVALID_DATE.getMessage());

        // Category errors (3xxx)
        assertEquals(3001, ErrorCode.CATEGORY_NOT_FOUND.getCode());
        assertEquals("Category not found", ErrorCode.CATEGORY_NOT_FOUND.getMessage());

        assertEquals(3002, ErrorCode.CATEGORY_IN_USE.getCode());
        assertEquals("Category is in use and cannot be deleted", ErrorCode.CATEGORY_IN_USE.getMessage());

        assertEquals(3003, ErrorCode.SYSTEM_CATEGORY_PROTECTED.getCode());
        assertEquals("System category cannot be modified", ErrorCode.SYSTEM_CATEGORY_PROTECTED.getMessage());
    }

    @Test
    @DisplayName("Server error codes should be in the 5xx range with correct messages")
    void testServerErrorCodes() {
        assertEquals(500, ErrorCode.INTERNAL_ERROR.getCode());
        assertEquals("Internal server error", ErrorCode.INTERNAL_ERROR.getMessage());

        assertEquals(503, ErrorCode.SERVICE_UNAVAILABLE.getCode());
        assertEquals("Service temporarily unavailable", ErrorCode.SERVICE_UNAVAILABLE.getMessage());
    }
}
