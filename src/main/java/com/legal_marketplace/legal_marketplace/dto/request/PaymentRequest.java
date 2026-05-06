package com.legal_marketplace.legal_marketplace.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class PaymentRequest {

    @Data
    public static class Create{
        @NotNull
        private UUID contractId;



        private String paymentReference;
    }
}
