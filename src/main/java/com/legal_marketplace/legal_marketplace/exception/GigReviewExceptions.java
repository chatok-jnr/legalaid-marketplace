package com.legal_marketplace.legal_marketplace.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class GigReviewExceptions {
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class DuplicateReviewException extends RuntimeException{
        public DuplicateReviewException() {
            super("You have already reviewed this gig");
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class GigReviewNotFoundException extends RuntimeException{
        public GigReviewNotFoundException() {
            super("Gig review not found");
        }
    }
}
