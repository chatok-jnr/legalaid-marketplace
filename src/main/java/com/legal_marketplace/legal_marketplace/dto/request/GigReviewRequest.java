package com.legal_marketplace.legal_marketplace.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

public class GigReviewRequest {
    @Builder
    @Data
    public static class Create {
        @NotNull
        private UUID gigId;
        // user id will be taken from the token, so no need to include it in the request body
        @NotNull
        @Min(1)
        @Max(5)
        private Integer rating;
        private String comment;
    }

}
