package com.legal_marketplace.legal_marketplace.repository.projectiions;

import com.legal_marketplace.legal_marketplace.entity.enums.ContractStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.OffsetDateTime;
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
