package com.legal_marketplace.legal_marketplace.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class BkashPaymentExceptions {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class NotFound extends RuntimeException {
        public NotFound() {
            super("bKash payment not found");
        }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class AlreadyExists extends RuntimeException {
        public AlreadyExists() {
            super("bKash payment for this contract payment already exists");
        }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class DuplicateTransactionId extends RuntimeException {
        public DuplicateTransactionId() {
            super("Transaction ID already exists");
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class BadRequest extends RuntimeException {
        public BadRequest(String message) {
            super(message);
        }
    }
}
