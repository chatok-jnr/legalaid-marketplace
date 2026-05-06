package com.legal_marketplace.legal_marketplace.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

public class ContractRequest {
    @Data
    public static class Crete{
        @NotNull
        private UUID gigId;
        private String requirements;
        @NotNull
        private Instant deliveryDeadline;
    }

    @Data
    @SuppressWarnings("unused")
    public static class Cancel{
        @NotBlank
        private String cancelledReason;
    }

}
