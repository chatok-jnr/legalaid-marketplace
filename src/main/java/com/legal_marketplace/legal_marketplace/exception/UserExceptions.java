package com.legal_marketplace.legal_marketplace.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class UserExceptions {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException() {
            super("User not found");
        }
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public static class AccessDeniedException extends RuntimeException {
        public AccessDeniedException() {
            super("Access denied");
        }
    }
}
