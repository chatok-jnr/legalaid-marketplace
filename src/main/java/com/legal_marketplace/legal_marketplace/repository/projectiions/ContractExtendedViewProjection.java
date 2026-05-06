package com.legal_marketplace.legal_marketplace.repository.projectiions;

import com.legal_marketplace.legal_marketplace.entity.enums.DisputeStatus;
import com.legal_marketplace.legal_marketplace.entity.enums.PaymentStatus;

import java.time.Instant;
import java.util.UUID;

public interface ContractExtendedViewProjection {
    UUID getId();
    String getClientName();
    String getLawyerName();
    UUID getGigId();
    String getGigTitle();
    UUID getClientId();
    UUID getLawyerId();
    Integer getPriceAtHire();
    String getRequirements();
    Integer getRevisionLeft();
    String getStatus();
    String getCancellationReason();
    Instant getCancelledAt();
    UUID getCancelledBy();
    Instant getDeliveryDeadline();
    Instant getCreatedAt();
    Instant getUpdatedAt();

    // Payment Info
    UUID getPaymentID();
    Integer getPlatformFeeAmount();
    Integer getLawyerPayoutAmount();
    PaymentStatus getPaymentStatus();
    String getPaymentReference();
    Instant getPaidAt();
    Instant getEscrowReleasedAt();


    // Delivery Info
    // This is a one to many relationship so use json_agg in the query to aggregate all deliveries related to this contract into a JSON array


    // Dispute Info
    UUID getDisputeId();
    DisputeStatus getDisputeStatus();
    String getDisputeReason();
    UUID getDisputeOpenedBy();
    UUID getDisputeAdminId();
    Instant getDisputeOpenedAt();
    Instant getDisputeResolvedAt();
    String getDisputeResolution();
}
