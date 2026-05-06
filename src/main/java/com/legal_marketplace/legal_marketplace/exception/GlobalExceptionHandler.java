package com.legal_marketplace.legal_marketplace.exception;

import com.legal_marketplace.legal_marketplace.dto.response.ErrorResponse;
import com.legal_marketplace.legal_marketplace.dto.response.ValidationErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // Domain Exception

    // ==================================
    // FIELD VALIDATION -----------------
    // ==================================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());
        Map<String, List<String>> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.computeIfAbsent(error.getField(), k -> new java.util.ArrayList<>())
                        .add(error.getDefaultMessage())
        );
        return validationResponse("Field validation failed", fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("Constraint violation: {}", ex.getMessage());
        Map<String, List<String>> fieldErrors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = extractFieldName(violation.getPropertyPath().toString());
            fieldErrors.computeIfAbsent(fieldName, k -> new java.util.ArrayList<>())
                    .add(violation.getMessage());
        });
        return validationResponse("Validation failed", fieldErrors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.error("Type mismatch: {}", ex.getMessage());
        Map<String, List<String>> fieldErrors = new HashMap<>();
        fieldErrors.put(ex.getName(), List.of("Invalid value"));
        return validationResponse("Invalid request parameter", fieldErrors);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ValidationErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        log.error("Missing request parameter: {}", ex.getMessage());
        Map<String, List<String>> fieldErrors = new HashMap<>();
        fieldErrors.put(ex.getParameterName(), List.of("Parameter is required"));
        return validationResponse("Missing required parameter", fieldErrors);
    }

    private ResponseEntity<ValidationErrorResponse> validationResponse(String message, Map<String, List<String>> fieldErrors) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ValidationErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        message,
                        fieldErrors
                ));
    }

    private String extractFieldName(String propertyPath) {
        int lastDot = propertyPath.lastIndexOf('.');
        return lastDot >= 0 ? propertyPath.substring(lastDot + 1) : propertyPath;
    }

    // ==================================
    // USER -----------------------------
    // ==================================
    @ExceptionHandler(UserExceptions.UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserExceptions.UserNotFoundException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        ex.getMessage(),
                        "will be updated soon"
                ));
    }

    @ExceptionHandler(UserExceptions.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(UserExceptions.AccessDeniedException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(
                        HttpStatus.UNAUTHORIZED.value(),
                        HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                        ex.getMessage(),
                        "will be updated soon"
                ));
    }

    // ==================================
    // GIG -----------------------------
    // ==================================
    @ExceptionHandler(GigExceptions.GigNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleGigNotFoundException(GigExceptions.GigNotFoundException ex) {
        log.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        ex.getMessage(),
                        "path will be updated soon"
                ));
    }

    // ==================================
    // Gig Media ------------------------
    // ==================================
    @ExceptionHandler(GigMediaExceptions.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleGigMediaNotFoundException(GigMediaExceptions.NotFoundException ex) {
        log.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        ex.getMessage(),
                        "path will be updated soon"
                ));
    }

    @ExceptionHandler(GigMediaExceptions.ConflictException.class)
    public ResponseEntity<ErrorResponse> handleGigMediaConflictException(GigMediaExceptions.ConflictException ex) {
        log.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(
                        HttpStatus.CONFLICT.value(),
                        HttpStatus.CONFLICT.getReasonPhrase(),
                        ex.getMessage(),
                        "path will be updated soon"
                ));
    }

    // ==================================
    // =======Contract
    // ==================================
    @ExceptionHandler(ContractExceptions.NotFound.class)
    public ResponseEntity<ErrorResponse> handleContractNotFoundException(ContractExceptions.NotFound ex) {
        log.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        "Contract not found",
                        "Path will be updated soon"
                ));
    }

    @ExceptionHandler(ContractExceptions.BadRequest.class)
    public ResponseEntity<ErrorResponse> handleContractBadRequest(ContractExceptions.BadRequest ex) {
        log.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "This contract can't be cancelled",
                        "path will be updated soon"
                ));
    }

    // ==================================
    // =======Contract Payment
    // ==================================

    @ExceptionHandler(PaymentExceptions.PaymentExists.class)
    public  ResponseEntity<ErrorResponse> handlePaymentExistsException(PaymentExceptions.PaymentExists ex) {
        log.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(
                        HttpStatus.CONFLICT.value(),
                        HttpStatus.CONFLICT.getReasonPhrase(),
                        ex.getMessage(),
                        "path will be updated soon"
                ));
    }

    // ==================================
    // =======Contract Delivery
    // ==================================
    @ExceptionHandler(ContractDeliveryExceptions.NotFound.class)
    public ResponseEntity<ErrorResponse> handleContractDeliveryNotFound(ContractDeliveryExceptions.NotFound ex) {
        log.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        ex.getMessage(),
                        "path will be updated soon"
                ));
    }

    @ExceptionHandler(ContractDeliveryExceptions.NoRevisionLeft.class)
    public ResponseEntity<ErrorResponse> handleNoRevisionLeft(ContractDeliveryExceptions.NoRevisionLeft ex) {
        log.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        ex.getMessage(),
                        "path will be updated soon"
                ));
    }

    @ExceptionHandler(ContractDeliveryExceptions.BadRequest.class)
    public ResponseEntity<ErrorResponse> handleContractDeliveryBadRequest(ContractDeliveryExceptions.BadRequest ex) {
        log.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        ex.getMessage(),
                        "path will be updated soon"
                ));
    }

    // ==================================
    // =======Contract Dispute
    // ==================================
    @ExceptionHandler(ContractDisputeExceptions.AlreadyExists.class)
    public ResponseEntity<ErrorResponse> handleContractDisputeAlreadyExists(ContractDisputeExceptions.AlreadyExists ex) {
        log.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(
                        HttpStatus.CONFLICT.value(),
                        HttpStatus.CONFLICT.getReasonPhrase(),
                        ex.getMessage(),
                        "path will be updated soon"
                ));
    }

    // =====================================
    // SECURITY ----------------------------
    // =====================================
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleSpringSecurityAccessDeniedException(AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(
                        HttpStatus.FORBIDDEN.value(),
                        HttpStatus.FORBIDDEN.getReasonPhrase(),
                        "You do not have permission to access this resource",
                        "will be updated soon"
                ));
    }
}
