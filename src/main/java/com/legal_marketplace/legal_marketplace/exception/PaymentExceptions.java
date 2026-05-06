package com.legal_marketplace.legal_marketplace.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class PaymentExceptions {
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class PaymentExists extends RuntimeException{
        public PaymentExists() {
            super("Payment for this contract already exists");
        }
    }
}
