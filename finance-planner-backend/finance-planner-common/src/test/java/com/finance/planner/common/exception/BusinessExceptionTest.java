package com.finance.planner.common.exception;

import com.finance.planner.common.constant.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BusinessException Tests")
class BusinessExceptionTest {

    @Test
    @DisplayName("Constructor with ErrorCode should set code and message from ErrorCode")
    void testConstructWithErrorCode() {
        BusinessException exception = new BusinessException(ErrorCode.USER_NOT_FOUND);

        assertEquals(1001, exception.getCode());
        assertEquals("User not found", exception.getMessage());
        // super(message) should also be set correctly
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    @DisplayName("Constructor with ErrorCode and custom message should use ErrorCode code but custom message")
    void testConstructWithErrorCodeAndMessage() {
        String customMessage = "User with id 456 was not found";

        BusinessException exception = new BusinessException(ErrorCode.USER_NOT_FOUND, customMessage);

        assertEquals(1001, exception.getCode());
        assertEquals(customMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Constructor with code and message should set both fields directly")
    void testConstructWithCodeAndMessage() {
        BusinessException exception = new BusinessException(9999, "Custom error occurred");

        assertEquals(9999, exception.getCode());
        assertEquals("Custom error occurred", exception.getMessage());
    }

    @Test
    @DisplayName("BusinessException should extend RuntimeException")
    void testExceptionIsRuntimeException() {
        BusinessException exception = new BusinessException(ErrorCode.INTERNAL_ERROR);

        assertInstanceOf(RuntimeException.class, exception);
        assertInstanceOf(Exception.class, exception);
        assertInstanceOf(Throwable.class, exception);

        // Verify it can be caught as RuntimeException
        assertThrows(RuntimeException.class, () -> {
            throw exception;
        });

        // Verify it can be caught as BusinessException
        assertThrows(BusinessException.class, () -> {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        });
    }
}
