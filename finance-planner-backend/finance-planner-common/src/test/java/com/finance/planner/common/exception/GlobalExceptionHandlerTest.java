package com.finance.planner.common.exception;

import com.finance.planner.common.constant.ErrorCode;
import com.finance.planner.common.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("handleBusinessException should return error response with exception code and message")
    void testHandleBusinessException() {
        BusinessException exception = new BusinessException(ErrorCode.USER_NOT_FOUND);

        ApiResponse<Void> response = handler.handleBusinessException(exception);

        assertEquals(1001, response.getCode());
        assertEquals("User not found", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("handleValidationException should return 422 with joined field error messages")
    void testHandleValidationException() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("object", "username", "Username is required");
        FieldError fieldError2 = new FieldError("object", "email", "Email is invalid");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        ApiResponse<Void> response = handler.handleValidationException(exception);

        assertEquals(ErrorCode.VALIDATION_ERROR.getCode(), response.getCode());
        assertTrue(response.getMessage().contains("Username is required"));
        assertTrue(response.getMessage().contains("Email is invalid"));
        assertNull(response.getData());
    }

    @Test
    @DisplayName("handleConstraintViolationException should return 422 with joined violation messages")
    @SuppressWarnings("unchecked")
    void testHandleConstraintViolationException() {
        Set<ConstraintViolation<?>> violations = new HashSet<>();

        ConstraintViolation<Object> violation1 = mock(ConstraintViolation.class);
        when(violation1.getMessage()).thenReturn("must not be null");

        ConstraintViolation<Object> violation2 = mock(ConstraintViolation.class);
        when(violation2.getMessage()).thenReturn("must be positive");

        violations.add(violation1);
        violations.add(violation2);

        ConstraintViolationException exception = new ConstraintViolationException(violations);

        ApiResponse<Void> response = handler.handleConstraintViolationException(exception);

        assertEquals(ErrorCode.VALIDATION_ERROR.getCode(), response.getCode());
        assertTrue(response.getMessage().contains("must not be null"));
        assertTrue(response.getMessage().contains("must be positive"));
        assertNull(response.getData());
    }

    @Test
    @DisplayName("handleMissingParameterException should return 400 with parameter name in message")
    void testHandleMissingParameterException() {
        MissingServletRequestParameterException exception =
                new MissingServletRequestParameterException("userId", "Long");

        ApiResponse<Void> response = handler.handleMissingParameterException(exception);

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), response.getCode());
        assertEquals("Missing required parameter: userId", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("handleHttpMessageNotReadableException should return 400 with fixed message")
    void testHandleHttpMessageNotReadableException() {
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);
        when(exception.getMessage()).thenReturn("JSON parse error");

        ApiResponse<Void> response = handler.handleHttpMessageNotReadableException(exception);

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), response.getCode());
        assertEquals("Invalid request body", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("handleMethodNotSupportedException should return 405 with METHOD_NOT_ALLOWED error")
    void testHandleMethodNotSupportedException() {
        HttpRequestMethodNotSupportedException exception =
                new HttpRequestMethodNotSupportedException("DELETE");

        ApiResponse<Void> response = handler.handleMethodNotSupportedException(exception);

        assertEquals(ErrorCode.METHOD_NOT_ALLOWED.getCode(), response.getCode());
        assertEquals(ErrorCode.METHOD_NOT_ALLOWED.getMessage(), response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("handleBadCredentialsException should return 1002 with INVALID_CREDENTIALS error")
    void testHandleBadCredentialsException() {
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");

        ApiResponse<Void> response = handler.handleBadCredentialsException(exception);

        assertEquals(ErrorCode.INVALID_CREDENTIALS.getCode(), response.getCode());
        assertEquals(ErrorCode.INVALID_CREDENTIALS.getMessage(), response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("handleException should return 500 with INTERNAL_ERROR for unexpected exceptions")
    void testHandleGenericException() {
        Exception exception = new RuntimeException("Something went terribly wrong");

        ApiResponse<Void> response = handler.handleException(exception);

        assertEquals(ErrorCode.INTERNAL_ERROR.getCode(), response.getCode());
        assertEquals(ErrorCode.INTERNAL_ERROR.getMessage(), response.getMessage());
        assertNull(response.getData());
    }
}
