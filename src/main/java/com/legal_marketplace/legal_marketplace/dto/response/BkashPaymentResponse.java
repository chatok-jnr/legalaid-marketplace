package com.legal_marketplace.legal_marketplace.dto.response;

import com.legal_marketplace.legal_marketplace.entity.enums.BkashPaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public class BkashPaymentResponse {
    @Data
    @Builder
    public static class Create {
        private UUID id;
        private UUID contractPaymentId;
        private String senderNumber;
        private String receiverNumber;
        private String transactionId;
        private BigDecimal amount;
        private BkashPaymentStatus status;
        private Instant createdAt;
        private Instant updatedAt;
    }

    @Data
    @Builder
    public static class Details {
        private UUID id;
        private UUID contractPaymentId;
        private UUID contractId;
        private UUID clientId;
        private UUID lawyerId;
        private String senderNumber;
        private String receiverNumber;
        private String transactionId;
        private BigDecimal amount;
        private BkashPaymentStatus status;
        private UUID verifiedBy;
        private Instant verifiedAt;
        private String rejectionReason;
        private Instant createdAt;
        private Instant updatedAt;
    }

    @Data
    @Builder
    public static class Verification {
        private UUID id;
        private UUID contractPaymentId;
        private BkashPaymentStatus status;
        private UUID verifiedBy;
        private Instant verifiedAt;
        private String rejectionReason;
        private Instant updatedAt;
    }

    // For admin
    @Data
    @Builder
    public static class BasicView{
        private UUID id;
        private BigDecimal amount;
        private String method;
        private Instant submitted;
        private BkashPaymentStatus status;
    }

    @Data
    @Builder
    public static class ExtendedView{
        private UUID id;

        private Integer platformFeeAmount;
        private Integer lawyerPayoutAmount;

        private String senderNumber;
        private String receiverNumber;
        private String transactionId;
        private BigDecimal amount;

        private BkashPaymentStatus status;

        private UUID verifiedBy;
        private Instant verifiedAt;

        private String rejectionReason;

        private Instant createdAt;
        private Instant updatedAt;
    }
}
