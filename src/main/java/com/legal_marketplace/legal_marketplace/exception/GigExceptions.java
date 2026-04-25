package com.legal_marketplace.legal_marketplace.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class GigExceptions {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class GigNotFoundException extends RuntimeException{
        public GigNotFoundException() {
            super("Gig not found");
        }
    }
}
