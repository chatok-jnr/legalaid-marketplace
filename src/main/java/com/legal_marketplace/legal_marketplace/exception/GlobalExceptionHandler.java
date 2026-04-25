package com.legal_marketplace.legal_marketplace.exception;

import jakarta.validation.executable.ValidateOnExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.legal_marketplace.legal_marketplace.dto.response.ErrorResponse;

import static org.aspectj.weaver.tools.cache.SimpleCacheFactory.path;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // Domain Exception

    // USER -----------------------------
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

    // GIG -----------------------------
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

    // Gig Media ---------------------------
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

    // SECURITY ----------------------------
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
