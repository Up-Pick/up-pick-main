package org.oneog.uppick.common.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;

import org.oneog.uppick.common.dto.GlobalApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleBusinessException(BusinessException e) {
        GlobalApiResponse<Void> response = GlobalApiResponse.fail(null,
            e.getMessage() + " (" + e.getErrorCode().getName() + ")");
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
        GlobalApiResponse<Void> response = GlobalApiResponse.fail(null, errorMessage.toString().trim());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleException(Exception e) {
        GlobalApiResponse<Void> response = GlobalApiResponse.fail(null, "Internal server error");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
