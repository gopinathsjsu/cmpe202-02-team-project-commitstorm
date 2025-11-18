package com.campus.marketplace.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {
    
    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;
    
    private ServletWebRequest mockRequest;
    
    @BeforeEach
    void setUp() {
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.setRequestURI("/api/test");
        mockRequest = new ServletWebRequest(httpRequest);
    }
    
    @Test
    void testHandleValidationExceptions() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        List<ObjectError> errors = new ArrayList<>();
        FieldError fieldError = new FieldError("object", "fieldName", "must not be blank");
        errors.add(fieldError);

        // Mock a BindingResult and have the exception return it so handler can read errors
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getAllErrors()).thenReturn(errors);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = globalExceptionHandler
            .handleValidationExceptions(ex, mockRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("VALIDATION_ERROR", response.getBody().getCode());
        assertNotNull(response.getBody().getRequestId());
    }
    
    @Test
    void testHandleRuntimeException() {
        RuntimeException ex = new RuntimeException("Test error message");
        
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = globalExceptionHandler
            .handleRuntimeException(ex, mockRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("BAD_REQUEST", response.getBody().getCode());
        assertEquals("Test error message", response.getBody().getMessage());
        assertNotNull(response.getBody().getRequestId());
    }
    
    @Test
    void testHandleRuntimeException_NullMessage() {
        RuntimeException ex = new RuntimeException();
        
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = globalExceptionHandler
            .handleRuntimeException(ex, mockRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An error occurred", response.getBody().getMessage());
    }
    
    @Test
    void testHandleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");
        
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = globalExceptionHandler
            .handleAccessDeniedException(ex, mockRequest);
        
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("FORBIDDEN", response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("Access denied"));
    }
    
    @Test
    void testHandleBadCredentialsException() {
        BadCredentialsException ex = new BadCredentialsException("Invalid credentials");
        
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = globalExceptionHandler
            .handleBadCredentialsException(ex, mockRequest);
        
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UNAUTHORIZED", response.getBody().getCode());
    }
    
    @Test
    void testErrorResponse_ContainsTimestamp() {
        RuntimeException ex = new RuntimeException("Test");
        
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = globalExceptionHandler
            .handleRuntimeException(ex, mockRequest);
        
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());
    }
    
    @Test
    void testErrorResponse_ContainsPath() {
        RuntimeException ex = new RuntimeException("Test");
        
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = globalExceptionHandler
            .handleRuntimeException(ex, mockRequest);
        
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getPath());
    }
}
