package com.legal_marketplace.legal_marketplace.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

public class GigRequest {
    @Data
    public static class CreateGig {
        @Length(max = 500)
        @NotNull
        private String title;
        private int maxRevision = 0;
        @Min(1)
        @NotNull
        private Integer minPrice;
        private String aboutThisGig;
        private boolean isPublic = true;
    }

    @Data
    public static class UpdateGig {
        @Length(max = 100)
        private String title;
        private int maxRevision;
        @Min(1)
        private Integer minPrice;
        private String aboutThisGig;
        private boolean isPublic;
    }
}
