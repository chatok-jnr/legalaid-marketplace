package com.legal_marketplace.legal_marketplace.dto.response;

import com.legal_marketplace.legal_marketplace.entity.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class PaymentResponse {

    @Data
    @Builder
    public static class Create{
        private UUID id;
        private UUID contractId;
        private int platformFeeAmount;
        private BigDecimal platformFeePercent;
        private int lawyerPayoutAmount;
        private PaymentStatus paymentStatus;
        private String paymentReference;
    }

    @Data
    @Builder
    public static class GetByContractId{
        private UUID id;
        private UUID contractId;
        private int platformFeeAmount;
        private BigDecimal platformFeePercent;
        private int lawyerPayoutAmount;

        private PaymentStatus paymentStatus;
        private String paymentReference;
        private Instant paidAt;
        private Instant escrowReleasedAt;
    }
}
