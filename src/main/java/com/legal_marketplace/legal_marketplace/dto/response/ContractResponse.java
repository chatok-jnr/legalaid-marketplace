package com.legal_marketplace.legal_marketplace.dto.response;

import com.legal_marketplace.legal_marketplace.entity.enums.ContractStatus;
import com.legal_marketplace.legal_marketplace.entity.enums.DisputeStatus;
import com.legal_marketplace.legal_marketplace.entity.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public class ContractResponse {

    @Data
    @Builder
    public static class Create{
        private UUID id;

        private UUID clientId;
        private UUID lawyerId;
        private UUID gigId;

        private Integer priceAtHire;
        private String requirements;
        private Integer revisionsLeft;

        private ContractStatus status;

        private Instant deliveryDeadline;
        private Instant createdAt;
    }

    @Data
    @Builder
    public static class ClientContractView{
        private String gigTitle;
        private String lawyerName;
        private String status;
        private String priceAtHire;
        private Instant createdAt;
    }

    @Data
    @Builder
    public static class ContractView{
        private UUID id;
        private String gigTitle;
        private String clientName;
        private String lawyerName;
        private Integer priceAtHire;
        private ContractStatus status;
        private Instant createdAt;
    }


    @Data
    @Builder
    public static class ContractExtendedView{
        private UUID id;
        private UUID clientId;
        private UUID lawyerId;
        private UUID gigId;

        private String clientName;
        private String lawyerName;

        private String gigTitle;
        private Integer priceAtHire;
        private String requirements;
        private Integer revisionsLeft;
        private ContractStatus status;

        private Instant cancelledAt;
        private String cancellationReason;
        private UUID cancelledBy;

        private Instant deliveryDeadline;
        private Instant createdAt;
        private Instant updatedAt;

        // Payment info
        private UUID paymentId;
        private Integer platformFeeAmount;
        private Integer lawyerPayoutAmount;
        private String paymentReference;
        private PaymentStatus paymentStatus;
        private Instant paidAt;
        private Instant escrowReleasedAt;

        // Dispute info
        private UUID disputeId;
        private DisputeStatus disputeStatus;
        private String disputeReason;
        private UUID disputeOpenedBy;
        private UUID disputeAdminId;
        private Instant disputeOpenedAt;
        private Instant disputeResolvedAt;
        private String disputeResolution;
    }
}
