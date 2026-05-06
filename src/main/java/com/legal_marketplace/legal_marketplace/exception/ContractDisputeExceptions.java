package com.legal_marketplace.legal_marketplace.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ContractDisputeExceptions {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class AlreadyExists extends RuntimeException {
        public AlreadyExists() {
            super("Dispute for this contract already exists");
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class BadRequest extends RuntimeException {
        public BadRequest(String message) {
            super(message);
        }
    }
}
