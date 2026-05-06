package com.legal_marketplace.legal_marketplace.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ValidationErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, List<String>> fieldErrors
) {
    public static ValidationErrorResponse of(int status, String error, String message, Map<String, List<String>> fieldErrors) {
        return new ValidationErrorResponse(LocalDateTime.now(), status, error, message, fieldErrors);
    }
}

