package com.legal_marketplace.legal_marketplace.service;

import com.legal_marketplace.legal_marketplace.dto.request.ContractDisputeRequest;
import com.legal_marketplace.legal_marketplace.dto.response.ContractDisputeResponse;
import com.legal_marketplace.legal_marketplace.entity.enums.DisputeStatus;

import java.util.List;
import java.util.UUID;

public interface ContractDisputeService {
    ContractDisputeResponse.Create create(ContractDisputeRequest.Create request, String userEmail);

    String assignAdmin(UUID disputeId, String adminEmail);
    String resolveDispute(UUID disputeId, String resolution, String adminEmail);

    List<ContractDisputeResponse.BasicView> getDisputes(DisputeStatus disputeStatus, String adminEmail);
    ContractDisputeResponse.ExtendedView getDisputesExtendedView(UUID disputeId);

}
