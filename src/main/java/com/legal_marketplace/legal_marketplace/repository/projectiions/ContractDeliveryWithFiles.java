package com.legal_marketplace.legal_marketplace.repository.projectiions;

import java.time.Instant;
import java.util.UUID;

public interface ContractDeliveryWithFiles {
    UUID getDeliveryId();

    UUID getClientId();

    UUID getLawyerId();

    UUID getContractId();

    String getDeliveryNote();

    Instant getDeliveredAt();

    Instant getRevisionRequestedAt();

    String getRevisionNote();

    Instant getCompletedAt();

    // Native query returns JSONB aggregate for file metadata.
    String getDeliveryFiles();
}
