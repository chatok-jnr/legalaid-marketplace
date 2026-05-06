package com.legal_marketplace.legal_marketplace.service;

import com.legal_marketplace.legal_marketplace.dto.request.ContractDisputeRequest;
import com.legal_marketplace.legal_marketplace.dto.response.ContractDisputeResponse;

public interface ContractDisputeService {
    ContractDisputeResponse.Create create(ContractDisputeRequest.Create request, String userEmail);
}
