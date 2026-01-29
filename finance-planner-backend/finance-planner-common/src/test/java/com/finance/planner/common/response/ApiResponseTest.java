package com.finance.planner.common.response;

import com.finance.planner.common.constant.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ApiResponse Tests")
class ApiResponseTest {

    @Test
    @DisplayName("success() should return response with code 200 and no data")
    void testSuccessNoData() {
        ApiResponse<Void> response = ApiResponse.success();

        assertEquals(200, response.getCode());
        assertEquals("success", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("success(data) should return response with code 200 and provided data")
    void testSuccessWithData() {
        String testData = "Hello World";

        ApiResponse<String> response = ApiResponse.success(testData);

        assertEquals(200, response.getCode());
        assertEquals("success", response.getMessage());
        assertEquals("Hello World", response.getData());
    }

    @Test
    @DisplayName("success(message, data) should return response with code 200, custom message, and data")
    void testSuccessWithMessage() {
        String customMessage = "Operation completed successfully";
        Integer testData = 42;

        ApiResponse<Integer> response = ApiResponse.success(customMessage, testData);

        assertEquals(200, response.getCode());
        assertEquals(customMessage, response.getMessage());
        assertEquals(42, response.getData());
    }

    @Test
    @DisplayName("error(ErrorCode) should return response with error code and default message")
    void testErrorWithErrorCode() {
        ApiResponse<Void> response = ApiResponse.error(ErrorCode.NOT_FOUND);

        assertEquals(404, response.getCode());
        assertEquals("Resource not found", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("error(ErrorCode, message) should return response with error code and custom message")
    void testErrorWithCustomMessage() {
        String customMessage = "User with id 123 not found";

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.USER_NOT_FOUND, customMessage);

        assertEquals(1001, response.getCode());
        assertEquals(customMessage, response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("error(code, message) should return response with provided code and message")
    void testErrorWithCodeAndMessage() {
        ApiResponse<Void> response = ApiResponse.error(9999, "Custom error");

        assertEquals(9999, response.getCode());
        assertEquals("Custom error", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("All constructors should set timestamp to a reasonable value")
    void testTimestampIsSet() {
        long before = System.currentTimeMillis();

        ApiResponse<Void> defaultResponse = new ApiResponse<>();
        ApiResponse<String> paramResponse = new ApiResponse<>(200, "ok", "data");
        ApiResponse<Void> successResponse = ApiResponse.success();
        ApiResponse<Void> errorResponse = ApiResponse.error(ErrorCode.INTERNAL_ERROR);

        long after = System.currentTimeMillis();

        // All timestamps should be between before and after
        assertTrue(defaultResponse.getTimestamp() >= before && defaultResponse.getTimestamp() <= after,
                "Default constructor timestamp should be within expected range");
        assertTrue(paramResponse.getTimestamp() >= before && paramResponse.getTimestamp() <= after,
                "Parameterized constructor timestamp should be within expected range");
        assertTrue(successResponse.getTimestamp() >= before && successResponse.getTimestamp() <= after,
                "Success factory timestamp should be within expected range");
        assertTrue(errorResponse.getTimestamp() >= before && errorResponse.getTimestamp() <= after,
                "Error factory timestamp should be within expected range");

        // All timestamps should be greater than zero
        assertTrue(defaultResponse.getTimestamp() > 0);
        assertTrue(paramResponse.getTimestamp() > 0);
        assertTrue(successResponse.getTimestamp() > 0);
        assertTrue(errorResponse.getTimestamp() > 0);
    }
}
