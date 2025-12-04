package com.campus.marketplace.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Global exception handler with consistent error response format.
 * Never exposes stack traces in production.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        String requestId = UUID.randomUUID().toString();
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        logger.warn("Validation failed [{}]: {}", requestId, errors);
        
        ErrorResponse response = new ErrorResponse(
            LocalDateTime.now(),
            getPath(request),
            "VALIDATION_ERROR",
            "Invalid input data",
            errors
        );
        response.setRequestId(requestId);
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        String requestId = UUID.randomUUID().toString();
        logger.error("Runtime exception [{}]: {}", requestId, ex.getMessage(), ex);
        
        ErrorResponse response = new ErrorResponse(
            LocalDateTime.now(),
            getPath(request),
            "BAD_REQUEST",
            ex.getMessage() != null ? ex.getMessage() : "An error occurred",
            null
        );
        response.setRequestId(requestId);
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        String requestId = UUID.randomUUID().toString();
        logger.warn("Access denied [{}]: {}", requestId, ex.getMessage());
        
        ErrorResponse response = new ErrorResponse(
            LocalDateTime.now(),
            getPath(request),
            "FORBIDDEN",
            "Access denied. You don't have permission to perform this action.",
            null
        );
        response.setRequestId(requestId);
        
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        String requestId = UUID.randomUUID().toString();
        logger.warn("Authentication failed [{}]: {}", requestId, ex.getMessage());
        
        ErrorResponse response = new ErrorResponse(
            LocalDateTime.now(),
            getPath(request),
            "UNAUTHORIZED",
            "Invalid credentials",
            null
        );
        response.setRequestId(requestId);
        
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        String requestId = UUID.randomUUID().toString();
        logger.warn("Authentication exception [{}]: {}", requestId, ex.getMessage());
        
        ErrorResponse response = new ErrorResponse(
            LocalDateTime.now(),
            getPath(request),
            "UNAUTHORIZED",
            "Authentication required",
            null
        );
        response.setRequestId(requestId);
        
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        String requestId = UUID.randomUUID().toString();
        logger.error("Unexpected error [{}]: {}", requestId, ex.getMessage(), ex);
        
        ErrorResponse response = new ErrorResponse(
            LocalDateTime.now(),
            getPath(request),
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred. Please try again later.",
            null
        );
        response.setRequestId(requestId);
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    private String getPath(WebRequest request) {
        if (request instanceof ServletWebRequest) {
            return ((ServletWebRequest) request).getRequest().getRequestURI();
        }
        return request.getDescription(false).replace("uri=", "");
    }
    
    /**
     * Standardized error response format.
     */
    public static class ErrorResponse {
        private String requestId;
        private LocalDateTime timestamp;
        private String path;
        private String code;
        private String message;
        private Object details;
        
        public ErrorResponse(LocalDateTime timestamp, String path, String code, String message, Object details) {
            this.timestamp = timestamp;
            this.path = path;
            this.code = code;
            this.message = message;
            this.details = details;
        }
        
        // Getters and setters
        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Object getDetails() { return details; }
        public void setDetails(Object details) { this.details = details; }
    }
}
