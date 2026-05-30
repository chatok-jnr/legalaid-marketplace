package com.legal_marketplace.legal_marketplace.repository.projections;

import com.legal_marketplace.legal_marketplace.entity.enums.DisputeStatus;

import java.util.UUID;

public interface ContractDisputeBasicViewProjection {
    UUID getId();
    DisputeStatus getDisputeStatus();
    UUID getContractId();
    Integer getPrice();
    String getTitle();
    UUID getDisputeOpenedBy();
    UUID getClientId();
}
