package com.legal_marketplace.legal_marketplace.dto.response;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jdk.dynalink.beans.StaticClass;
import lombok.Builder;
import lombok.Data;

import java.awt.print.Pageable;
import java.time.OffsetDateTime;
import java.util.UUID;

public class GigResponse {

    @Data
    @Builder
    public static class MyGig {
        private UUID id;
        private UUID lawyerId;
        private String title;
        private int minPrice;
        private String aboutThisGig;
        private boolean isPublic;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
    }

    @Data
    @Builder
    public static class OtherGig {
        private UUID lawyerId;
        private String title;
        private int minPrice;
        private String aboutThisGig;
    }
}
