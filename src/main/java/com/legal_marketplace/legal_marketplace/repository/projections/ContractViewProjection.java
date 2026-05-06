package com.legal_marketplace.legal_marketplace.repository.projections;

import com.legal_marketplace.legal_marketplace.entity.enums.ContractStatus;

import java.time.Instant;
import java.util.UUID;

public interface ContractViewProjection {
    UUID getId();
    String getClientName();
    String getLawyerName();
    String getGigTitle();
    int getPriceAtHire();
    ContractStatus getStatus();
    Instant getCreatedAt();
}
