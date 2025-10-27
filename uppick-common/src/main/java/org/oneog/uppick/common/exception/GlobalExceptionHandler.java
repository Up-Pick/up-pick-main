package org.oneog.uppick.common.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.oneog.uppick.common.dto.GlobalApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.validation.FieldError;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("Business exception occurred: {}", e.getMessage(), e);
        GlobalApiResponse<Void> response = GlobalApiResponse.fail(null,
            e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        StringBuilder errorMessage = new StringBuilder("Validation errors: ");
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError)error).getField();
            String errorMsg = error.getDefaultMessage();
            errorMessage.append(fieldName).append(": ").append(errorMsg).append("; ");
        });
        log.warn("Validation failed: {}", errorMessage.toString().trim(), ex);
        GlobalApiResponse<Void> response = GlobalApiResponse.fail(null, errorMessage.toString().trim());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleMethodArgumentTypeMismatchException(
        MethodArgumentTypeMismatchException e) {
        String message = "Invalid value for parameter '" + e.getName() + "': " + e.getValue();
        log.warn("Type mismatch for parameter: {}", e.getName(), e);
        GlobalApiResponse<Void> response = GlobalApiResponse.fail(null, message);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException e) {
        log.warn("Malformed JSON request received", e);
        GlobalApiResponse<Void> response = GlobalApiResponse.fail(null, "Malformed JSON request");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleHttpRequestMethodNotSupportedException(
        HttpRequestMethodNotSupportedException e) {
        String message = "Request method '" + e.getMethod() + "' not supported for this endpoint";
        log.warn("Unsupported HTTP method: {} for endpoint", e.getMethod(), e);
        GlobalApiResponse<Void> response = GlobalApiResponse.fail(null, message);
        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleMissingServletRequestParameterException(
        MissingServletRequestParameterException e) {
        String message = "Required parameter '" + e.getParameterName() + "' is missing";
        log.warn("Missing required parameter: {}", e.getParameterName(), e);
        GlobalApiResponse<Void> response = GlobalApiResponse.fail(null, message);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        StringBuilder errorMessage = new StringBuilder("Constraint violations: ");
        e.getConstraintViolations().forEach(violation -> {
            errorMessage.append(violation.getPropertyPath()).append(": ").append(violation.getMessage()).append("; ");
        });
        log.warn("Constraint violations occurred: {}", errorMessage.toString().trim(), e);
        GlobalApiResponse<Void> response = GlobalApiResponse.fail(null, errorMessage.toString().trim());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Access denied for user", e);
        GlobalApiResponse<Void> response = GlobalApiResponse.fail(null, "Access denied");
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleHttpMediaTypeNotSupportedException(
        HttpMediaTypeNotSupportedException e) {
        String message = "Unsupported media type: " + e.getContentType() + ". Supported types: "
            + e.getSupportedMediaTypes();
        log.warn("Unsupported media type: {} for endpoint", e.getContentType(), e);
        GlobalApiResponse<Void> response = GlobalApiResponse.fail(null, message);
        return new ResponseEntity<>(response, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleMultipartException(MultipartException e) {
        log.warn("Multipart upload error: {}", e.getMessage(), e);
        GlobalApiResponse<Void> response = GlobalApiResponse.fail(null, "Multipart upload error: " + e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("Resource not found: {}", e.getMessage(), e);
        GlobalApiResponse<Void> response = GlobalApiResponse.fail(null, "Resource not found");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleException(Exception e) {
        log.error("Internal server error", e);
        GlobalApiResponse<Void> response = GlobalApiResponse.fail(null, "Internal server error");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
