package com.legal_marketplace.legal_marketplace.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ContractDeliveryExceptions {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class NotFound extends RuntimeException {
        public NotFound() { super("Contract Delivery not found"); }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class NoRevisionLeft extends RuntimeException {
        public NoRevisionLeft() { super("No revisions left for this delivery"); }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class BadRequest extends RuntimeException {
        public BadRequest(String message) {
            super(message);
        }
    }
}

